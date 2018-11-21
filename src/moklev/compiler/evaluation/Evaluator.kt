package moklev.compiler.evaluation

import moklev.compiler.exceptions.EvaluationException
import moklev.compiler.exceptions.ReturnException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*
import moklev.compiler.types.ClassType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class Evaluator : SomeEvaluator {
    val variableState = mutableListOf(mutableMapOf<String, Value>())

    override fun evaluateArrayPointerShiftOperation(element: ArrayPointerShiftOperation): Value {
        val arrayPointer = evaluateExpression(element.arrayPointer) as Value.ArrayPointer 
        val shift = evaluateExpression(element.shift)
        return arrayPointer.shift(shift.int64Value)
    }

    override fun evaluateAddressOf(element: AddressOf): Value {
        return object : Value.Pointer() {
            override val sourceType: Type
                get() = element.target.type

            override fun write(value: Value) {
                assign(element.target, value)
            }

            override fun read(): Value {
                return evaluateExpression(element.target)
            }
        }
    }

    override fun evaluateDereference(element: Dereference): Value {
        val target = evaluateExpression(element.target)
        if (target !is Value.Pointer)
            throw EvaluationException(element, "Can't dereference a non-pointer value: $target")
        return target.read()
    }

    override fun evaluateFieldReference(element: FieldReference): Value {
        val target = evaluateExpression(element.target) as? Value.Class
                ?: throw EvaluationException(element, "Target should be a class value")
        if (target.type.declaration.fields.find { it.name == element.name } == null)
            throw EvaluationException(element, "No field `${element.name}` in class `${target.type.declaration.name}`")
        return target.fields[element.name] ?: throw EvaluationException(element, "Uninitialized field")
    }

    private fun invokeFunction(target: FunctionReference, parameters: List<Value>) {
        if (target.declaration.body === SemanticStatement.Stub) {
            val result = Predefined.invokeFunction(target.declaration.name, parameters)
            throw ReturnException(result)
        } 
        Evaluator().apply {
            withScope {
                val offset = if (target is MethodReference) 1 else 0
                parameters.forEachIndexed { i, value ->
                    if (target is MethodReference && i == 0) {
                        initializeLocalVariable("this", value)
                    } else {
                        initializeLocalVariable(target.declaration.parameters[i - offset].first, value)
                    }
                }
                evaluateStatement(target.declaration.body)
            }
        }
    }
    
    override fun evaluateInvocation(element: Invocation): Value {
        when (val target = element.target) {
            is ConstructorReference -> {
                return Value.Class(ClassType(target.declaration)).apply {
                    val parameters = element.parameters.map { evaluateExpression(it) }
                    for (index in 0 until target.declaration.fields.size) {
                        fields[target.declaration.fields[index].name] = parameters[index]
                    }
                }
            }
            is FunctionReference -> {
                try {
                    val parameters = element.parameters.map { evaluateExpression(it) }
                    invokeFunction(target, parameters)
                    throw EvaluationException(element, "Function ${target.declaration.name} returned no value")
                } catch (e: ReturnException) {
                    return e.value
                }
            }
            else -> throw EvaluationException(element, "Unknown target: `${target.javaClass}`")
        }
    }

    override fun evaluateReturn(element: Return) {
        val value = evaluateExpression(element.value)
        throw ReturnException(value)
    }

    override fun evaluateIf(element: If) {
        val condition = { evaluateExpression(element.condition).booleanValue }
        if (condition()) {
            withScope {
                evaluateStatement(element.bodyTrue)
            }
        } else {
            withScope {
                evaluateStatement(element.bodyFalse)
            }
        }
    }

    override fun evaluateWhile(element: While) {
        val condition = { evaluateExpression(element.condition).booleanValue }
        while (condition()) {
            withScope {
                evaluateStatement(element.body)
            }
        }
    }

    override fun evaluateLocalVariableReference(element: LocalVariableReference): Value {
        return variableState[element.scopeLevel][element.name]
                ?: throw EvaluationException(element, "Variable is not initialized: ${element.name}") 
    }

    override fun evaluateAssignment(element: Assignment) {
        val value = evaluateExpression(element.value)
        assign(element.target, value)
    }

    private fun assign(target: SemanticExpression, value: Value) {
        if (value.type != target.type)
            throw EvaluationException("Type mismatch: ${value.type} and ${target.type}")
        when (target) {
            is LocalVariableReference -> assignLocalVariable(target, value)
            is Dereference -> assignDereference(target, value)
            is FieldReference -> assignFieldReference(target, value)
            else -> throw EvaluationException("Left side of expression is not an assignable expression: $target")
        }
    }

    private fun assignFieldReference(target: FieldReference, value: Value) {
        val valueTarget = evaluateExpression(target.target) as? Value.Class
                ?: throw EvaluationException(target, "Target is not a class value")
        valueTarget.fields[target.name] = value
    }
    
    private fun assignDereference(target: Dereference, value: Value) {
        val innerTarget = evaluateExpression(target.target) as? Value.Pointer 
                ?: throw EvaluationException("Can't write to dereference of a non-pointer value")
        innerTarget.write(value)
    }
    
    private fun assignLocalVariable(target: LocalVariableReference, value: Value) {
        variableState[target.scopeLevel][target.name] = value
    }
    
    override fun evaluateStatementList(element: StatementList) {
        element.statements.forEach { statement ->
            evaluateStatement(statement)
        }
    }

    override fun evaluateVariableDeclaration(element: VariableDeclaration) = Unit

    override fun evaluateInt64Constant(element: Int64Constant): Value {
        return Value.Int64(element.value)
    }

    override fun evaluateDoubleConstant(element: DoubleConstant): Value {
        return Value.Double(element.value)
    }

    override fun evaluateBooleanBinaryOperation(element: BooleanBinaryOperation): Value {
        val left = lazy { evaluateExpression(element.left) }
        val right = lazy { evaluateExpression(element.right) }
        return when (element.op) {
            "&&" -> if (!left.value.booleanValue) Value.Boolean(false) else right.value 
            "||" -> if (left.value.booleanValue) Value.Boolean(true) else right.value 
            else -> throw EvaluationException(element, "Unknown op for BooleanBinaryOperation: \"${element.op}\"")
        }
    }

    override fun evaluateInt64BinaryOperation(element: Int64BinaryOperation): Value {
        val left = evaluateExpression(element.left)
        val right = evaluateExpression(element.right)
        return when (element.op) {
            "+" -> Value.Int64(left.int64Value + right.int64Value)
            "-" -> Value.Int64(left.int64Value - right.int64Value)
            "*" -> Value.Int64(left.int64Value * right.int64Value)
            "/" -> Value.Int64(left.int64Value / right.int64Value)
            "==" -> Value.Boolean(left.int64Value == right.int64Value)
            "<" -> Value.Boolean(left.int64Value < right.int64Value)
            else -> throw EvaluationException(element, "Unknown op for Int64BinaryOperation: \"${element.op}\"")
        }
    }

    override fun evaluateFunctionDeclaration(element: FunctionDeclaration) = Unit

    override fun evaluateClassDeclaration(element: ClassDeclaration) = Unit

    override fun evaluateFieldDeclaration(element: FieldDeclaration) = Unit

    override fun evaluateFunctionReference(element: FunctionReference): Value {
        throw EvaluationException(element, "Function reference can't be used as a value")
    }

    override fun evaluateConstructorReference(element: ConstructorReference): Value {
        throw EvaluationException(element, "Constructor reference can't be used as a value")
    }

    private inline fun <T> withScope(body: () -> T): T {
        variableState.add(mutableMapOf())
        try {
            return body()
        } finally {
            variableState.removeAt(variableState.lastIndex)
        }
    }
    
    private fun initializeLocalVariable(name: String, value: Value) {
        val scope = variableState.last()
        scope[name] = value
    }
}