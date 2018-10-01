package moklev.compiler.compilation

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.impl.VariableReference
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class SymbolResolver {
    val declaredVariables = mutableMapOf<String, Type>()
    
    fun resolveSymbol(name: String): SemanticExpression {
        val declaredType = declaredVariables.get(name)
                ?: throw CompilationException("Unresolved symbol: $name")
        return VariableReference(name, declaredType)
    }
    
    fun declareVariable(name: String, type: Type) {
        if (name in declaredVariables)
            throw CompilationException("Already declared variable: $name")
        declaredVariables[name] = type
    }
}