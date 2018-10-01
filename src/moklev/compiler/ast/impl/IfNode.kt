package moklev.compiler.ast.impl

import moklev.compiler.ast.ExpressionASTNode
import moklev.compiler.ast.StatementASTNode

/**
 * @author Moklev Vyacheslav
 */
class IfNode(
        val condition: ExpressionASTNode, 
        val bodyTrue: StatementASTNode, 
        val bodyFalse: StatementASTNode 
) : StatementASTNode