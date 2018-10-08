package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.PointerType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class Dereference(val target: SemanticExpression) : SemanticExpression {
    override val type: Type
        get() = target.type.let { targetType ->
            if (targetType is PointerType)
                targetType.sourceType
            else
                throw CompilationException(this, "Type of target is not a pointer type: $targetType")
        }
}