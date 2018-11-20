package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticDeclaration
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class FieldDeclaration(val name: String, val type: Type) : SemanticDeclaration