package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.Type

class ConstructorReference(val declaration: ClassDeclaration) : SemanticExpression {
    override val type: Type
        get() = throw CompilationException(this, "Constructor reference has no valid type")
}