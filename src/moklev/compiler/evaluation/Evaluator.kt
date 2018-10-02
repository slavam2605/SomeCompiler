package moklev.compiler.evaluation

import moklev.compiler.exceptions.EvaluationException
import moklev.compiler.exceptions.ReturnException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*

/**
 * @author Moklev Vyacheslav
 */
class Evaluator {
    val variableState = mutableListOf(mutableMapOf<String, Value>())
    
    fun evaluateStatement(statement: SemanticStatement) {
        if (statement is Assignment)
            return evaluateAssignment(statement)
        if (statement is StatementList)
            return evaluateStatementList(statement)
        if (statement is VariableDeclaration)
            return evaluateVariableDeclaration(statement)
        if (statement is While)
            return evaluateWhile(statement)
        if (statement is If)
            return evaluateIf(statement)
        if (statement is Return)
            return evaluateReturn(statement)
        if (statement is SemanticExpression)
            return evaluateExpression(statement).let { Unit }
        throw EvaluationException(statement, "Unknown semantic statement: $statement")
    }
    
    fun evaluateExpression(expression: SemanticExpression): Value {
        if (expression is Int64Constant)
            return evaluateInt64Constant(expression)
        if (expression is DoubleConstant)
            return evaluateDoubleConstant(expression)
        if (expression is Int64BinaryOperation)
            return evaluateInt64BinaryOperation(expression)
        if (expression is LocalVariableReference)
            return evaluateVariableReference(expression)
        if (expression is Invocation)
            return evaluateInvocation(expression)
        throw EvaluationException(expression, "Unknown semantic expression: $expression")
    }
    
    fun evaluateInvocation(element: Invocation): Value {
        try {
            val target = element.target as FunctionReference
            val parameters = element.parameters.map { evaluateExpression(it) }
            Evaluator().apply {
                withScope {
                    parameters.forEachIndexed { i, value ->
                        initializeLocalVariable(target.declaration.parameters[i].first, value)
                    }
                    evaluateStatement(target.declaration.body)
                }
            }
            throw EvaluationException(element, "Function ${target.declaration.name} returned no value")
        } catch (e: ReturnException) {
            return e.value
        }
    }
    
    fun evaluateReturn(element: Return) {
        val value = evaluateExpression(element.value)
        throw ReturnException(value)
    }
    
    fun evaluateIf(element: If) {
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
    
    fun evaluateWhile(element: While) {
        val condition = { evaluateExpression(element.condition).booleanValue }
        while (condition()) {
            withScope {
                evaluateStatement(element.body)
            }
        }
    }
    
    fun evaluateVariableReference(element: LocalVariableReference): Value {
        return variableState[variableState.lastIndex - element.scopeLevel][element.name]
                ?: throw EvaluationException(element, "Variable is not initialized: ${element.name}") 
    }
    
    fun evaluateAssignment(element: Assignment) {
        val value = evaluateExpression(element.value)
        val target = element.target
        if (target !is LocalVariableReference)
            throw EvaluationException(element, "Left side of expression is not an assignable expression: $target")
        if (value.type != target.type)
            throw EvaluationException(element, "Type mismatch: ${value.type} and ${target.type}")
        variableState[variableState.lastIndex - target.scopeLevel][target.name] = value
    }
    
    fun evaluateStatementList(element: StatementList) {
        element.statements.forEach { statement ->
            evaluateStatement(statement)
        }
    }
    
    fun evaluateVariableDeclaration(element: VariableDeclaration) = Unit
    
    fun evaluateInt64Constant(element: Int64Constant): Value {
        return Value.Int64(element.value)
    }
    
    fun evaluateDoubleConstant(element: DoubleConstant): Value {
        return Value.Double(element.value)
    }
    
    fun evaluateInt64BinaryOperation(element: Int64BinaryOperation): Value {
        val left = evaluateExpression(element.left)
        val right = evaluateExpression(element.right)
        return when (element.op) {
            "+" -> Value.Int64(left.int64Value + right.int64Value)
            "==" -> Value.Boolean(left.int64Value == right.int64Value)
            "<" -> Value.Boolean(left.int64Value < right.int64Value)
            else -> throw EvaluationException(element, "Unknown op for Int64BinaryOperation: \"${element.op}\"")
        }
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