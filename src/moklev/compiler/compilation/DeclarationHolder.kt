package moklev.compiler.compilation

import moklev.compiler.semantic.SemanticDeclaration

/**
 * @author Moklev Vyacheslav
 */
interface DeclarationHolder {
    fun declare(declaration: SemanticDeclaration)
}