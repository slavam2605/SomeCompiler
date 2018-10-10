package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class Int64BinaryOperation(val op: String, val left: SemanticExpression, val right: SemanticExpression) : SemanticExpression {
    override val type: Type
    
    init {
        when (op) {
            "+", "-", "*", "/" -> type = ScalarType.INT64
            "==", "<" -> type = ScalarType.BOOLEAN
            else -> throw CompilationException(this, "Unsupported binary operation for Int64: $op")
        }
    }
}