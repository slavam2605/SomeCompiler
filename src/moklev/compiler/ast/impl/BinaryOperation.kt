package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode

/**
 * @author Moklev Vyacheslav
 */
class BinaryOperation(val op: String, val left: ExpressionASTNode, val right: ExpressionASTNode) : ExpressionASTNode