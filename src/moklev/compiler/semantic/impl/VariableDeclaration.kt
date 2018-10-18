package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.annotation.BasicStatement
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
@BasicStatement
class VariableDeclaration(val name: String, val type: Type) : SemanticStatement