package moklev.compiler.semantic.impl

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticDeclaration
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
open class FunctionDeclaration(
        val name: String,
        val parameters: List<Pair<String, Type>>,
        val returnType: Type
) : SemanticDeclaration {
    private var isStub: Boolean = true

    var body: SemanticStatement = SemanticStatement.Stub
        get() {
            if (isStub)
                throw CompilationException(this, "This is stub element")
            return field
        }
        private set(value) {
            if (!isStub)
                throw CompilationException(this, "Can't modify non-stub element")
            field = value
        }
    
    fun complete(body: SemanticStatement) {
        this.body = body
        isStub = false
    }
}