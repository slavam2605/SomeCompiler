package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.PointerType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class AddressOf(val target: SemanticExpression) : SemanticExpression {
    override val type: Type
        get() = PointerType(target.type)
}