package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode

/**
 * @author Moklev Vyacheslav
 */
class DereferenceNode(val target: ExpressionASTNode) : ExpressionASTNode