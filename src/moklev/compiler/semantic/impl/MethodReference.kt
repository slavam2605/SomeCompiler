package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.ClassType

/**
 * @author Moklev Vyacheslav
 */
class MethodReference(val name: String, val target: SemanticExpression) : FunctionReference((target.type as ClassType).declaration.methods.find { it.name == name }
        ?: throw CompilationException("No method '$name' in class '${target.type}'")) {
}

val MethodReference.targetType
    get() = target.type as ClassType