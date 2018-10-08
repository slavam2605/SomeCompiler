package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class ArrayPointerShiftOperation(val op: String, val arrayPointer: SemanticExpression, val shift: SemanticExpression) : SemanticExpression {
    override val type: Type
    
    init {
        when (op) {
            "+", "-" -> type = arrayPointer.type
            else -> throw CompilationException(this, "Unsupported array pointer shift operation: $op")
        }
    }
}