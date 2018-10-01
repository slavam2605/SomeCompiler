package moklev.compiler.semantic

import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
interface SemanticExpression : SemanticStatement {
    val type: Type
}