package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode

/**
 * @author Moklev Vyacheslav
 */
class BinaryOperationNode(val op: String, val left: ExpressionASTNode, val right: ExpressionASTNode) : ExpressionASTNode