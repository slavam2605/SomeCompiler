package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode
import moklev.compiler.ast.StatementASTNode

/**
 * @author Moklev Vyacheslav
 */
class WhileNode(val condition: ExpressionASTNode, val body: StatementASTNode) : StatementASTNode