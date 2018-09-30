package moklev.compiler.ast.impl

import moklev.compiler.ast.SemanticASTNode
import moklev.compiler.semantic.SemanticElement
import moklev.compiler.semantic.impl.Int64Constant

/**
 * @author Moklev Vyacheslav
 */
class Constant(val value: String) : SemanticASTNode {
    override fun toSemanticElement(): SemanticElement =
            Int64Constant(value.toLong())
}