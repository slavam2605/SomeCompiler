package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class Int64Constant(val value: Long) : SemanticExpression {
    override val type: Type = ScalarType.INT64
}