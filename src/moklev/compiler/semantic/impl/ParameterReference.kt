package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.Type

class ParameterReference(val name: String, override val type: Type) : SemanticExpression