package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class BooleanBinaryOperation(val op: String, val left: SemanticExpression, val right: SemanticExpression) : SemanticExpression {
    override val type: Type = when (op) {
        "&&", "||" -> ScalarType.BOOLEAN
        else -> throw CompilationException(this, "Unsupported binary operation for Boolean: $op")
    }
}