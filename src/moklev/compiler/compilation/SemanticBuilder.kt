package moklev.compiler.compilation

import moklev.compiler.ast.DeclarationASTNode
import moklev.compiler.ast.impl.*
import moklev.compiler.compilation.DiagnosticCompilationErrors.AddressOfError
import moklev.compiler.compilation.DiagnosticCompilationErrors.AssignTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.DereferenceTypeError
import moklev.compiler.compilation.DiagnosticCompilationErrors.IfConditionBooleanTypeError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationArgumentTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationTargetError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationWrongNumberOfArgumentsError
import moklev.compiler.compilation.DiagnosticCompilationErrors.NoSuchBinaryOperationError
import moklev.compiler.compilation.DiagnosticCompilationErrors.ReturnTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.WhileConditionBooleanTypeError
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*
import moklev.compiler.types.ArrayPointerType
import moklev.compiler.types.PointerType
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class SemanticBuilder : SomeBuilder {
    val typeResolver = TypeResolver()
    val symbolResolver = SymbolResolver()
    val semanticAnalyzer = SemanticAnalyzer()
    var currentFunction: FunctionDeclaration? = null

    private inline fun <T> withFunction(function: FunctionDeclaration, body: () -> T): T {
        currentFunction = function
        try {
            return body()
        } finally {
            currentFunction = null
        }
    }
    
    override fun buildAddressOf(node: AddressOfNode): SemanticExpression {
        val target = buildExpression(node.target)
        if (target is LocalVariableReference)
            return AddressOf(target)
        throw CompilationException(node, AddressOfError(target))
    }

    override fun buildDereference(node: DereferenceNode): SemanticExpression {
        val target = buildExpression(node.target)
        if (target.type is PointerType)
            return Dereference(target)
        throw CompilationException(node, DereferenceTypeError(target.type))
    }

    override fun buildArrayPointerType(node: ArrayPointerTypeNode): Type {
        val sourceType = buildType(node.sourceType)
        return ArrayPointerType(sourceType)
    }

    override fun buildPointerType(node: PointerTypeNode): Type {
        val sourceType = buildType(node.sourceType)
        return PointerType(sourceType)
    }

    override fun buildScalarType(node: ScalarTypeNode): Type {
        return typeResolver.resolveType(node.name)
    }

    fun buildDeclarationStub(root: DeclarationASTNode) {
        if (root is FunctionDeclarationNode)
            return buildFunctionDeclarationStub(root)
        throw CompilationException(root, "Not a declaration ASTNode: $root")
    }
    
    override fun buildInvocation(node: InvocationNode): SemanticExpression {
        val target = buildExpression(node.target)
        if (target !is FunctionReference)
            throw CompilationException(node, InvocationTargetError(target))
        val parameters = node.parameters.map { buildExpression(it) }
        if (parameters.size != target.declaration.parameters.size)
            throw CompilationException(node, InvocationWrongNumberOfArgumentsError(target.declaration.parameters.size, parameters.size))
        for (index in 0 until parameters.size) {
            if (parameters[index].type != target.declaration.parameters[index].second)
                throw CompilationException(node, InvocationArgumentTypeMismatchError(index, target.declaration.parameters[index].second, parameters[index].type))
        }
        return Invocation(target, parameters)
    }

    override fun buildReturn(node: ReturnNode): SemanticStatement {
        val value = buildExpression(node.value)
        val function = currentFunction
            ?: throw CompilationException(node, "No current function")
        if (value.type != function.returnType)
            throw CompilationException(node, ReturnTypeMismatchError(function.returnType, value.type))
        return Return(value)
    }
    
    fun buildFunctionDeclarationStub(node: FunctionDeclarationNode) {
        val parameters = node.parameters.map { (name, type) -> name to buildType(type) }
        val returnType = buildType(node.returnType)
        val declarationStub = FunctionDeclaration(node.name, parameters, returnType)
        symbolResolver.declareFunction(declarationStub)
    }

    override fun buildFunctionDeclaration(node: FunctionDeclarationNode) {
        val stub = symbolResolver.declaredFunctions[node.name] 
                ?: throw CompilationException(node, "No predeclared stub found") 
        val body = withFunction(stub) {
            symbolResolver.withScope {
                stub.parameters.forEach { (name, type) ->
                    symbolResolver.declareVariable(name, type)
                }
                buildStatement(node.body)
            }
        }
        stub.complete(body)
        semanticAnalyzer.analyzeFunctionDeclaration(stub)
    }

    override fun buildDeclarationList(node: DeclarationListNode) {
        for (declaration in node.declarations)
            buildDeclarationStub(declaration)
        for (declaration in node.declarations)
            buildDeclaration(declaration)
    }

    override fun buildIf(node: IfNode): SemanticStatement {
        val condition = buildExpression(node.condition)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, IfConditionBooleanTypeError(condition.type))
        val bodyTrue = symbolResolver.withScope {
            buildStatement(node.bodyTrue)
        }
        val bodyFalse = symbolResolver.withScope {
            buildStatement(node.bodyFalse)
        }
        return If(condition, bodyTrue, bodyFalse)
    }

    override fun buildWhile(node: WhileNode): SemanticStatement {
        val condition = buildExpression(node.condition)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, WhileConditionBooleanTypeError(condition.type))
        val body = symbolResolver.withScope {
            buildStatement(node.body)
        }
        return While(condition, body)
    }

    override fun buildSymbol(node: SymbolNode): SemanticExpression {
        return symbolResolver.resolveSymbol(node.name)
    }

    override fun buildAssignment(node: AssignmentNode): Assignment {
        val value = buildExpression(node.value)
        val target = buildExpression(node.target)
        if (value.type != target.type)
            throw CompilationException(node, AssignTypeMismatchError(target.type, value.type))
        return Assignment(target, value)
    }

    override fun buildStatementList(node: StatementListNode): StatementList {
        val statements = node.statements.map { buildStatement(it) }
        return StatementList(statements)
    }

    override fun buildVariableDeclaration(node: VariableDeclarationNode): VariableDeclaration {
        val type = buildType(node.type)
        symbolResolver.declareVariable(node.name, type)
        return VariableDeclaration(node.name, type)
    }

    override fun buildConstant(node: ConstantNode): SemanticExpression {
        node.value.toLongOrNull()?.let { 
            return Int64Constant(it)
        }
        node.value.toDoubleOrNull()?.let { 
            return DoubleConstant(it)
        }
        throw CompilationException(node, "Unknown constant: \"${node.value}\"")
    }

    override fun buildBinaryOperation(node: BinaryOperationNode): SemanticExpression {
        val left = buildExpression(node.left)
        val right = buildExpression(node.right)
        if (left.type is ArrayPointerType && right.type == ScalarType.INT64) {
            return ArrayPointerShiftOperation(node.op, left, right)
        }
        if (left.type != right.type)
            throw CompilationException(node, NoSuchBinaryOperationError(node.op, left.type, right.type))
        when (left.type) {
            ScalarType.INT64 -> return Int64BinaryOperation(node.op, left, right)
            ScalarType.BOOLEAN -> return BooleanBinaryOperation(node.op, left, right)
            else -> throw CompilationException(node, NoSuchBinaryOperationError(node.op, left.type, right.type))
        }
    }
}