package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
open class FunctionReference(val declaration: FunctionDeclaration) : SemanticExpression {
    override val type: Type
        get() = throw CompilationException(this, "Function reference has no valid type") 
    
    val returnType: Type = declaration.returnType
}