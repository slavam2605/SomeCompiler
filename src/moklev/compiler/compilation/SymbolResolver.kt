package moklev.compiler.compilation

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.impl.FunctionDeclaration
import moklev.compiler.semantic.impl.FunctionReference
import moklev.compiler.semantic.impl.LocalVariableReference
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class SymbolResolver {
    val declaredVariables = mutableListOf(mutableMapOf<String, Type>())
    val declaredFunctions = mutableMapOf<String, FunctionDeclaration>()
    
    fun resolveSymbol(name: String): SemanticExpression {
        declaredVariables.asReversed().forEachIndexed { scopeIndex, scope ->
            val declaredType = scope[name] ?: return@forEachIndexed
            return LocalVariableReference(name, declaredType, scopeIndex)
        }
        declaredFunctions[name]?.let { declaredFunction ->
            return FunctionReference(declaredFunction)
        }
        throw CompilationException("Unresolved symbol: $name")
    }
    
    fun declareVariable(name: String, type: Type) {
        val lastScope = declaredVariables.last()
        if (name in lastScope)
            throw CompilationException("Already declared variable: $name")
        lastScope[name] = type
    }

    inline fun <T> withScope(body: () -> T): T {
        declaredVariables.add(mutableMapOf())
        try {
            return body()
        } finally {
            declaredVariables.removeAt(declaredVariables.lastIndex)
        }
    }
    
    fun declareFunction(declaration: FunctionDeclaration) {
        if (declaration.name in declaredFunctions)
            throw CompilationException("Function ${declaration.name} is already defined")
        declaredFunctions[declaration.name] = declaration
    }
    
    fun getFunction(name: String): FunctionDeclaration? {
        return declaredFunctions[name]
    }
}