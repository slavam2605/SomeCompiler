package moklev.compiler.backendCompilation.x86

import moklev.compiler.backendCompilation.SomeBackendCompiler
import moklev.compiler.backendCompilation.x86.targetSpecific.TargetSpecificTool
import moklev.compiler.compilation.ClassDeclarationContext
import moklev.compiler.compilation.CompilationContext
import moklev.compiler.compilation.FunctionBodyContext
import moklev.compiler.compilation.MethodBodyContext
import moklev.compiler.exceptions.BackendCompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*
import moklev.compiler.types.ClassType
import moklev.compiler.types.Type

class X86BackendCompiler(private val targetTool: TargetSpecificTool) : SomeBackendCompiler {
    val builder = X86AssemblyBuilder()
    private var stackOffset = 0
    private val declaredVariables = mutableListOf(mutableMapOf<String, X86StaticValue>())
    private var lastLocalLabel = 0

    private fun getLocalLabel(): String {
        lastLocalLabel++
        return ".L$lastLocalLabel"
    }

    private inline fun <T> withScope(body: () -> T): T {
        declaredVariables.add(mutableMapOf())
        try {
            return body()
        } finally {
            val totalStackSize = declaredVariables.last().map { (it.value as? X86LocalStackElement)?.size ?: 0 }.sum()
            stackOffset -= totalStackSize
            declaredVariables.removeAt(declaredVariables.lastIndex)
        }
    }

    private inline fun <T> withFunctionScope(parameters: List<Pair<String, Type>>, body: () -> T): T = withScope {
        // TODO: fix for double and other types parameters
        for (index in 0 until parameters.size) {
            declaredVariables.last()[parameters[index].first] = targetTool.parameterLocation(index)
        }
        return body()
    }

    private fun getVariableLocation(name: String): X86StaticValue {
        for (variableMap in declaredVariables.asReversed()) {
            variableMap[name]?.let {
                return it
            }
        }
        throw Exception("Variable not found: $name")
    }

    override fun compileAddressOf(element: AddressOf, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileArrayPointerShiftOperation(element: ArrayPointerShiftOperation, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileAssignment(element: Assignment, context: CompilationContext) {
        when (val target = element.target) {
            is LocalVariableReference -> {
                val location = declaredVariables[target.scopeLevel][target.name]!!
                compileExpression(element.value, context)
                builder.apply {
                    pop(RAX)
                    mov(location, RAX)
                }
            }
            else -> TODO("not implemented")
        }
    }

    override fun compileBooleanBinaryOperation(element: BooleanBinaryOperation, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileClassDeclaration(element: ClassDeclaration, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileConstructorReference(element: ConstructorReference, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileDereference(element: Dereference, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileDoubleConstant(element: DoubleConstant, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileFieldDeclaration(element: FieldDeclaration, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileFieldReference(element: FieldReference, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileFunctionDeclaration(element: FunctionDeclaration, context: CompilationContext) {
        require(stackOffset == 0)
        val labelName = targetTool.makeLabel((context as? ClassDeclarationContext)?.outerClass, element.name)
        builder.apply {
            directiveGlobal(labelName)
            label(labelName)
            push(RBP)
            mov(RBP, RSP)
        }
        val bodyContext = when (context) {
            is ClassDeclarationContext -> MethodBodyContext(ClassType(context.outerClass), element)
            else -> FunctionBodyContext(element)
        }
        withFunctionScope(element.parameters) {
            compileStatement(element.body, bodyContext)
        }
    }

    override fun compileFunctionReference(element: FunctionReference, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileIf(element: If, context: CompilationContext) {
        compileExpression(element.condition, context)
        val labelFalse = getLocalLabel()
        val labelAfter = getLocalLabel()
        builder.apply {
            pop(RAX)
            cmp(RAX, X86IntegerLiteral("0"))
            j("e", labelFalse)
            compileStatement(element.bodyTrue, context)
            jmp(labelAfter)
            label(labelFalse)
            compileStatement(element.bodyFalse, context)
            label(labelAfter)
        }
    }

    override fun compileInt64BinaryOperation(element: Int64BinaryOperation, context: CompilationContext) {
        compileExpression(element.left, context)
        compileExpression(element.right, context)
        builder.apply {
            pop(R10)
            pop(RAX)
            when (element.op) {
                "+" -> add(RAX, R10)
                "-" -> sub(RAX, R10)
                "*" -> imul(RAX, R10)
                "/" -> {
                    cqo()
                    idiv(R10)
                }
                "==" -> {
                    cmp(RAX, R10)
                    set("e", AL)
                    movzx(RAX, AL)
                }
                "<" -> {
                    cmp(RAX, R10)
                    set("l", AL)
                    movzx(RAX, AL)
                }
            }
            push(RAX)
        }
    }

    override fun compileInt64Constant(element: Int64Constant, context: CompilationContext) {
        builder.push(X86IntegerLiteral(element.value.toString(), 8))
    }

    override fun compileInvocation(element: Invocation, context: CompilationContext) {
        when (val target = element.target) {
            is FunctionReference -> {
                val functionLabel = targetTool.makeLabel(null, target.declaration.name)
                element.parameters.forEachIndexed { index, parameter ->
                    compileExpression(parameter, context)
                    builder.pop(targetTool.parameterLocation(index))
                }
                val saveLocations = mutableListOf<X86StaticValue>()
                for (variableMap in declaredVariables) {
                    for ((_, location) in variableMap) {
                        if (location in targetTool.volatileRegisters && location !in saveLocations) {
                            saveLocations.add(location)
                        }
                    }
                }
                builder.apply {
                    saveLocations.forEach { push(it) }
                    call(functionLabel)
                    saveLocations.asReversed().forEach { pop(it) }
                    push(RAX)
                }
            }
            else -> throw BackendCompilationException(element, "Not supported target: $target")
        }
    }

    override fun compileLocalVariableReference(element: LocalVariableReference, context: CompilationContext) {
        val variableLocation = getVariableLocation(element.name)
        return builder.push(variableLocation)
    }

    override fun compileReturn(element: Return, context: CompilationContext) {
        compileExpression(element.value, context)
        builder.apply {
            pop(RAX)
            mov(RSP, RBP)
            pop(RBP)
            ret()
        }
    }

    override fun compileStatementList(element: StatementList, context: CompilationContext) {
        for (statement in element.statements) {
            compileStatement(statement, context)
        }
    }

    override fun compileVariableDeclaration(element: VariableDeclaration, context: CompilationContext) {
        val machineSize = machineSizeOf(element.type)
        stackOffset += machineSize
        declaredVariables.last()[element.name] = X86LocalStackElement(stackOffset, machineSize)
        builder.sub(RSP, X86IntegerLiteral("$machineSize"))
    }

    override fun compileWhile(element: While, context: CompilationContext) {
        val labelStart = getLocalLabel()
        val labelAfter = getLocalLabel()
        builder.apply {
            label(labelStart)
            compileExpression(element.condition, context)
            pop(RAX)
            cmp(RAX, X86IntegerLiteral("0"))
            j("e", labelAfter)
            compileStatement(element.body, context)
            jmp(labelStart)
            label(labelAfter)
        }
    }

    override fun compileStatement(root: SemanticStatement, context: CompilationContext) {
        return when (root) {
            is SemanticExpression -> {
                compileExpression(root, context)
                builder.add(RSP, X86IntegerLiteral("8"))
            }
            else -> super.compileStatement(root, context)
        }
    }
}