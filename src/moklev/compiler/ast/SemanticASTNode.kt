package moklev.compiler.ast

import moklev.compiler.semantic.SemanticElement

/**
 * @author Moklev Vyacheslav
 */
interface SemanticASTNode : ASTNode {
    fun toSemanticElement(): SemanticElement
}