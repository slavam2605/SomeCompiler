package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class DoubleConstant(val value: Double) : SemanticExpression {
    override val type: Type = ScalarType.DOUBLE
}