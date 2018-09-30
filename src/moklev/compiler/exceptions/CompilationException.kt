package moklev.compiler.exceptions

import moklev.compiler.ast.ASTNode

/**
 * @author Moklev Vyacheslav
 */
class CompilationException(
        val astNode: ASTNode?,
        message: String
) : RuntimeException(message) {
    constructor(message: String) : this(null, message)
}