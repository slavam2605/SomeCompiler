package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode

/**
 * @author Moklev Vyacheslav
 */
class QualifiedSymbolNode(val target: ExpressionASTNode?, val name: String) : ExpressionASTNode