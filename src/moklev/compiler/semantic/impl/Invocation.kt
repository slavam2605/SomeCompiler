package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class Invocation(val target: SemanticExpression, val parameters: List<SemanticExpression>) : SemanticExpression {
    override val type: Type
        get() {
            if (target is FunctionReference)
                return target.returnType
            throw CompilationException(this, "Invocation target is not a function reference")
        }
}