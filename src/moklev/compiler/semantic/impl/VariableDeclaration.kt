package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class VariableDeclaration(val name: String, val type: Type) : SemanticStatement