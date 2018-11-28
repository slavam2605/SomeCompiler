package moklev.compiler.compilation

import moklev.compiler.compilation.DiagnosticCompilationErrors.ClassAlreadyDeclaredError
import moklev.compiler.compilation.DiagnosticCompilationErrors.FunctionAlreadyDeclaredError
import moklev.compiler.compilation.DiagnosticCompilationErrors.UnresolvedSymbolError
import moklev.compiler.compilation.DiagnosticCompilationErrors.VariableAlreadyDeclaredError
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticDeclaration
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*
import moklev.compiler.types.*

/**
 * @author Moklev Vyacheslav
 */
class SymbolResolver : DeclarationHolder {
    val declaredVariables = mutableListOf(mutableMapOf<String, Type>())
    val declaredFunctions = mutableMapOf<String, FunctionDeclaration>()
    val declaredClasses = mutableMapOf<String, ClassDeclaration>()
    val predefinedFunctions = mutableMapOf<String, FunctionDeclaration>()
    val functionBaseScope = mutableListOf<Int>()
    
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
    
    fun resolveSymbol(target: SemanticExpression?, name: String, context: CompilationContext): SemanticExpression {
        if (target == null)
            return resolveGlobalSymbol(name, context)
        return resolveQualifiedSymbol(target, name)
    }

    private fun resolveQualifiedSymbol(target: SemanticExpression, name: String): SemanticExpression {
        val targetType = target.type
        if (targetType is ClassType) {
            val declaration = targetType.declaration
            declaration.fields.find { it.name == name }?.let {
                return FieldReference(name, target)
            }
            declaration.methods.find { it.name == name }?.let {
                return MethodReference(name, target)
            }
        }
        throw CompilationException(UnresolvedSymbolError(name))
    }
    
    private fun resolveGlobalSymbol(name: String, context: CompilationContext): SemanticExpression {
        if (context is MethodBodyContext && name == "this") {
            return LocalVariableReference("this", PointerType(context.targetType), functionBaseScope.last())
        }
        declaredVariables.asReversed().forEachIndexed { scopeIndex, scope ->
            val declaredType = scope[name] ?: return@forEachIndexed
            val scopeLevel = declaredVariables.size - scopeIndex - 1
            return if (scopeLevel == 1 &&
                    context is FunctionBodyContext &&
                    context.declaration.parameters.find { it.first == name } != null)
                ParameterReference(name, declaredType)
            else
                LocalVariableReference(name, declaredType, scopeLevel)
        }
        declaredFunctions[name]?.let { declaredFunction ->
            return FunctionReference(declaredFunction)
        }
        declaredClasses[name]?.let { declaredClass -> // constructor reference
            return ConstructorReference(declaredClass)
        }
        predefinedFunctions[name]?.let { predefinedFunction ->
            return FunctionReference(predefinedFunction)
        }
        throw CompilationException(UnresolvedSymbolError(name))
    }
    
    fun declareVariable(name: String, type: Type) {
        val lastScope = declaredVariables.last()
        if (name in lastScope)
            throw CompilationException(VariableAlreadyDeclaredError(name))
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

    inline fun <T> withFunctionScope(body: () -> T): T {
        declaredVariables.add(mutableMapOf())
        functionBaseScope.add(declaredVariables.lastIndex)
        try {
            return body()
        } finally {
            declaredVariables.removeAt(declaredVariables.lastIndex)
            functionBaseScope.removeAt(functionBaseScope.lastIndex)
        }
    }

    override fun declare(declaration: SemanticDeclaration) {
        if (declaration is FunctionDeclaration)
            return declareFunction(declaration)
        if (declaration is ClassDeclaration)
            return declareClass(declaration)
        TODO("Make a generator for declarations visitor")
    }
    
    fun declareClass(declaration: ClassDeclaration) {
        if (declaration.name in declaredClasses)
            throw CompilationException(ClassAlreadyDeclaredError(declaration.name))
        declaredClasses[declaration.name] = declaration
    }
    
    fun declareFunction(declaration: FunctionDeclaration) {
        if (declaration.name in declaredFunctions)
            throw CompilationException(FunctionAlreadyDeclaredError(declaration.name))
        declaredFunctions[declaration.name] = declaration
    }
}