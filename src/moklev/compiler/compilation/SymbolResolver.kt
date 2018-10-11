package moklev.compiler.compilation

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.FunctionDeclaration
import moklev.compiler.semantic.impl.FunctionReference
import moklev.compiler.semantic.impl.LocalVariableReference
import moklev.compiler.types.ArrayPointerType
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class SymbolResolver {
    val declaredVariables = mutableListOf(mutableMapOf<String, Type>())
    val declaredFunctions = mutableMapOf<String, FunctionDeclaration>()
    val predefinedFunctions = mutableMapOf<String, FunctionDeclaration>()
    
    init {
        addPredefinedFunction(
                "createInt64Array", 
                listOf("n" to ScalarType.INT64), 
                ArrayPointerType(ScalarType.INT64)
        )
        addPredefinedFunction(
                "debugPrint",
                listOf("x" to ScalarType.INT64),
                ScalarType.INT64
        )
    }
    
    private fun addPredefinedFunction(name: String, parameters: List<Pair<String, Type>>, returnType: Type) {
        predefinedFunctions[name] = FunctionDeclaration(name, parameters, returnType).apply { 
            complete(SemanticStatement.Stub)
        }
    }
    
    fun resolveSymbol(name: String): SemanticExpression {
        declaredVariables.asReversed().forEachIndexed { scopeIndex, scope ->
            val declaredType = scope[name] ?: return@forEachIndexed
            val scopeLevel = declaredVariables.size - scopeIndex - 1
            return LocalVariableReference(name, declaredType, scopeLevel)
        }
        declaredFunctions[name]?.let { declaredFunction ->
            return FunctionReference(declaredFunction)
        }
        predefinedFunctions[name]?.let { predefinedFunction ->
            return FunctionReference(predefinedFunction)
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
}