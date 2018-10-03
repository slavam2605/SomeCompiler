package moklev.compiler.compilation

import moklev.compiler.ast.ASTNode
import moklev.compiler.ast.DeclarationASTNode
import moklev.compiler.ast.ExpressionASTNode
import moklev.compiler.ast.StatementASTNode
import moklev.compiler.ast.impl.*
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*
import moklev.compiler.types.ScalarType

/**
 * @author Moklev Vyacheslav
 */
class SemanticBuilder {
    val typeResolver = TypeResolver()
    val symbolResolver = SymbolResolver()
    
    fun build(root: ASTNode) {
        if (root is DeclarationListNode)
            return buildDeclarationList(root)
        throw CompilationException(root, "Unknown top level ASTNode: $root")
    }
    
    fun buildDeclaration(root: DeclarationASTNode) {
        if (root is FunctionDeclarationNode)
            return buildFunctionDeclaration(root)
        throw CompilationException(root, "Not a declaration ASTNode: $root")
    }

    fun buildDeclarationStub(root: DeclarationASTNode) {
        if (root is FunctionDeclarationNode)
            return buildFunctionDeclarationStub(root)
        throw CompilationException(root, "Not a declaration ASTNode: $root")
    }
    
    fun buildStatement(root: StatementASTNode): SemanticStatement {
        if (root is AssignmentNode)
            return buildAssignment(root)
        if (root is StatementListNode)
            return buildStatementList(root)
        if (root is VariableDeclarationNode)
            return buildVariableDeclaration(root)
        if (root is WhileNode)
            return buildWhile(root)
        if (root is IfNode)
            return buildIf(root)
        if (root is ReturnNode)
            return buildReturn(root)
        if (root is ExpressionASTNode)
            return buildExpression(root)
        throw CompilationException(root, "Not a statement ASTNode: $root")
    }
    
    fun buildExpression(root: ExpressionASTNode): SemanticExpression {
        if (root is ConstantNode)
            return buildConstant(root)
        if (root is BinaryOperationNode)
            return buildBinaryOperation(root)
        if (root is SymbolNode)
            return buildSymbol(root)
        if (root is InvocationNode)
            return buildInvocation(root)
        throw CompilationException(root, "Not an expression ASTNode: $root")
    }
    
    fun buildInvocation(node: InvocationNode): SemanticExpression {
        val target = buildExpression(node.target)
        if (target !is FunctionReference)
            throw CompilationException(node, "Invocation target is not a function reference: $target")
        val parameters = node.parameters.map { buildExpression(it) }
        return Invocation(target, parameters)
    }
    
    fun buildReturn(node: ReturnNode): SemanticStatement {
        val value = buildExpression(node.value)
        return Return(value)
    }
    
    fun buildFunctionDeclarationStub(node: FunctionDeclarationNode) {
        val parameters = node.parameters.map { (name, type) -> name to typeResolver.resolveType(type) }
        val returnType = typeResolver.resolveType(node.returnType)
        val declarationStub = FunctionDeclaration(node.name, parameters, returnType)
        symbolResolver.declareFunction(declarationStub)
    }
    
    fun buildFunctionDeclaration(node: FunctionDeclarationNode) {
        val stub = symbolResolver.getFunction(node.name)
            ?: throw CompilationException(node, "No predeclared stub found")
        val body = symbolResolver.withScope {
            stub.parameters.forEach { (name, type) ->
                symbolResolver.declareVariable(name, type)
            }
            buildStatement(node.body)
        }
        stub.complete(body)
    }
    
    fun buildDeclarationList(node: DeclarationListNode) {
        for (declaration in node.declarations)
            buildDeclarationStub(declaration)
        for (declaration in node.declarations)
            buildDeclaration(declaration)
    }
    
    fun buildIf(node: IfNode): SemanticStatement {
        val condition = buildExpression(node.condition)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, "Condition of `if` statement should have Boolean type, found: ${condition.type}")
        val bodyTrue = symbolResolver.withScope {
            buildStatement(node.bodyTrue)
        }
        val bodyFalse = symbolResolver.withScope {
            buildStatement(node.bodyFalse)
        }
        return If(condition, bodyTrue, bodyFalse)
    }
    
    fun buildWhile(node: WhileNode): SemanticStatement {
        val condition = buildExpression(node.condition)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, "Condition of `while` statement should have Boolean type, found: ${condition.type}")
        val body = symbolResolver.withScope {
            buildStatement(node.body)
        }
        return While(condition, body)
    }
    
    fun buildSymbol(node: SymbolNode): SemanticExpression {
        return symbolResolver.resolveSymbol(node.name)
    }
    
    fun buildAssignment(node: AssignmentNode): Assignment {
        val value = buildExpression(node.value)
        val target = buildExpression(node.target)
        return Assignment(target, value)
    }
    
    fun buildStatementList(node: StatementListNode): StatementList {
        val statements = node.statements.map { buildStatement(it) }
        return StatementList(statements)
    }
    
    fun buildVariableDeclaration(node: VariableDeclarationNode): VariableDeclaration {
        val type = typeResolver.resolveType(node.type)
        symbolResolver.declareVariable(node.name, type)
        return VariableDeclaration(node.name, type)
    }
    
    fun buildConstant(node: ConstantNode): SemanticExpression {
        node.value.toLongOrNull()?.let { 
            return Int64Constant(it)
        }
        node.value.toDoubleOrNull()?.let { 
            return DoubleConstant(it)
        }
        throw CompilationException(node, "Unknown constant: \"${node.value}\"")
    }
    
    fun buildBinaryOperation(node: BinaryOperationNode): SemanticExpression {
        val left = buildExpression(node.left)
        val right = buildExpression(node.right)
        if (left.type != right.type)
            throw CompilationException(node, "Types of left and right operands are different: ${left.type} and ${right.type}")
        when (left.type) {
            ScalarType.INT64 -> return Int64BinaryOperation(node.op, left, right) 
            else -> throw CompilationException(node, "Unknown type of operands: ${left.type}")
        }
    }
}