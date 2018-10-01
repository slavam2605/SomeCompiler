package moklev.compiler.compilation

import moklev.compiler.ast.ASTNode
import moklev.compiler.ast.ExpressionASTNode
import moklev.compiler.ast.StatementASTNode
import moklev.compiler.ast.impl.*
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticElement
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
    
    fun build(root: ASTNode): SemanticElement {
        if (root is StatementASTNode)
            return buildStatement(root)
        throw CompilationException(root, "Unknown ASTNode: $root")
    }
    
    fun buildStatement(root: StatementASTNode): SemanticStatement {
        if (root is AssignmentNode)
            return buildAssignment(root)
        if (root is StatementListNode)
            return buildStatementList(root)
        if (root is VariableDeclarationNode)
            return buildVariableDeclaration(root)
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
        throw CompilationException(root, "Not an expression ASTNode: $root")
    }
    
    fun buildSymbol(node: SymbolNode): SemanticExpression {
        return symbolResolver.resolveSymbol(node.name)
    }
    
    fun buildAssignment(node: AssignmentNode): Assignment {
        val value = buildExpression(node.value)
        return Assignment(node.variableName, value)
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