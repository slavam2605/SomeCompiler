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

class X86BackendCompiler(val targetTool: TargetSpecificTool) : SomeBackendCompiler {
    val builder = X86AssemblyBuilder()

    override fun compileAddressOf(element: AddressOf, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileArrayPointerShiftOperation(element: ArrayPointerShiftOperation, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileAssignment(element: Assignment, context: CompilationContext) {
        TODO("not implemented")
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
        val labelName = targetTool.makeLabel((context as? ClassDeclarationContext)?.outerClass, element.name)
        builder.apply {
            directiveGlobal(labelName)
            label(labelName)
        }
        val bodyContext = when (context) {
            is ClassDeclarationContext -> MethodBodyContext(ClassType(context.outerClass), element)
            else -> FunctionBodyContext(element)
        }
        compileStatement(element.body, bodyContext)
    }

    override fun compileFunctionReference(element: FunctionReference, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileIf(element: If, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileInt64BinaryOperation(element: Int64BinaryOperation, context: CompilationContext) {
        compileExpression(element.left, context)
        compileExpression(element.right, context)
        builder.apply {
            pop(RCX)
            pop(RAX)
            when (element.op) {
                "+" -> add(RAX, RCX)
                "-" -> sub(RAX, RCX)
                "*" -> imul(RAX, RCX)
                "/" -> {
                    cqo()
                    idiv(RCX)
                }
                "==" -> {
                    cmp(RAX, RCX)
                    set("e", AL)
                    movzx(RAX, AL)
                }
                "<" -> {
                    cmp(RAX, RCX)
                    set("l", AL)
                    movzx(RAX, AL)
                }
            }
            push(RAX)
        }
    }

    override fun compileInt64Constant(element: Int64Constant, context: CompilationContext) {
        builder.push(IntegerLiteral(element.value.toString(), 8))
    }

    override fun compileInvocation(element: Invocation, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileLocalVariableReference(element: LocalVariableReference, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileParameterReference(element: ParameterReference, context: CompilationContext) {
        if (context !is FunctionBodyContext)
            throw BackendCompilationException(element, "Parameter reference must be in function boyd context, found: '$context'")
        val parameterIndex = context.declaration.parameters.indexOfFirst { it.first == element.name }
        if (parameterIndex == -1)
            throw BackendCompilationException(element, "No parameter '${element.name} was declared in current context")
        val location = targetTool.parameterLocation(parameterIndex)
        // TODO fix push for stack arguments
        builder.push(location)
    }

    override fun compileReturn(element: Return, context: CompilationContext) {
        compileExpression(element.value, context)
        builder.apply {
            pop(RAX)
            ret()
        }
    }

    override fun compileStatementList(element: StatementList, context: CompilationContext) {
        for (statement in element.statements) {
            compileStatement(statement, context)
        }
    }

    override fun compileVariableDeclaration(element: VariableDeclaration, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileWhile(element: While, context: CompilationContext) {
        TODO("not implemented")
    }

    override fun compileStatement(root: SemanticStatement, context: CompilationContext) {
        return when (root) {
            is SemanticExpression -> {
                compileExpression(root, context)
                builder.add(RSP, IntegerLiteral("8"))
            }
            else -> super.compileStatement(root, context)
        }
    }
}