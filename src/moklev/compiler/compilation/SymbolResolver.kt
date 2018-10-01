package moklev.compiler.compilation

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.impl.LocalVariableReference
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class SymbolResolver {
    val declaredVariables = mutableListOf(mutableMapOf<String, Type>())
    
    fun resolveSymbol(name: String): SemanticExpression {
        declaredVariables.asReversed().forEachIndexed { scopeIndex, scope ->
            val declaredType = scope[name] ?: return@forEachIndexed
            return LocalVariableReference(name, declaredType, scopeIndex)
        }
        throw CompilationException("Unresolved symbol: $name")
    }
    
    fun declareVariable(name: String, type: Type) {
        val lastScope = declaredVariables.last()
        if (name in lastScope)
            throw CompilationException("Already declared variable: $name")
        lastScope[name] = type
    }
    
    fun enterScope() {
        declaredVariables.add(mutableMapOf())
    }
    
    fun leaveScope() {
        declaredVariables.removeAt(declaredVariables.lastIndex)
    }
    
    inline fun <T> withScope(body: () -> T): T {
        enterScope()
        try {
            return body()
        } finally {
            leaveScope()
        }
    }
}