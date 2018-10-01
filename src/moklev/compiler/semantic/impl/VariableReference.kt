package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class VariableReference(val name: String, override val type: Type) : SemanticExpression