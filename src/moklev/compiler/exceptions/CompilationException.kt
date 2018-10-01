package moklev.compiler.exceptions

import moklev.compiler.ast.ASTNode
import moklev.compiler.semantic.SemanticElement

/**
 * @author Moklev Vyacheslav
 */
class CompilationException private constructor(
        val astNode: ASTNode?,
        val semanticElement: SemanticElement?,
        message: String
) : RuntimeException(message) {
    constructor(astNode: ASTNode, message: String) : this(astNode, null, message)
    constructor(semanticElement: SemanticElement, message: String) : this(null, semanticElement, message)
    constructor(message: String) : this(null, null, message)
}