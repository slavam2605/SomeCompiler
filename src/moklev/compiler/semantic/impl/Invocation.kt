package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.ClassType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class Invocation(val target: SemanticExpression, val parameters: List<SemanticExpression>) : SemanticExpression {
    override val type: Type
        get() {
            return when (target) {
                is FunctionReference -> target.returnType
                is ConstructorReference -> ClassType(target.declaration)
                else -> throw CompilationException(this, "Invocation target is not a function or constructor reference")
            }
        }
}