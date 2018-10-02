package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode

/**
 * @author Moklev Vyacheslav
 */
class InvocationNode(val target: ExpressionASTNode, val parameters: List<ExpressionASTNode>) : ExpressionASTNode