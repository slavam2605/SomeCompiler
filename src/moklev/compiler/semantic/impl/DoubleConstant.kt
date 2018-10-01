package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticElement
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class DoubleConstant(val value: Double) : SemanticElement {
    override val type: Type = ScalarType.DOUBLE
}