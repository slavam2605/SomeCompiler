package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.ClassType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class FieldReference(val name: String, val target: SemanticExpression) : SemanticExpression {
    override val type: Type
        get() = (target.type as ClassType).declaration.fields.find { it.name == name }?.type
                ?: throw CompilationException(this, "No field '$name' in class '${target.type}'")
}