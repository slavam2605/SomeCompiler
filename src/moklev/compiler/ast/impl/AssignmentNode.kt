package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode
import moklev.compiler.ast.StatementASTNode

/**
 * @author Moklev Vyacheslav
 */
class AssignmentNode(val variableName: String, val value: ExpressionASTNode) : StatementASTNode