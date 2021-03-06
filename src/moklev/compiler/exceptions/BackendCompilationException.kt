package moklev.compiler.exceptions

import moklev.compiler.semantic.SemanticElement

/**
 * @author Moklev Vyacheslav
 */
class BackendCompilationException(
        val semanticElement: SemanticElement?,
        message: String
) : RuntimeException(message) {
    constructor(message: String) : this(null, message)
}