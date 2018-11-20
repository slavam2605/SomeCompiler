package moklev.compiler.compilation

import moklev.compiler.compilation.DiagnosticCompilationErrors.UnresolvedTypeError
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.types.ClassType
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class TypeResolver(val symbolResolver: SymbolResolver) {
    fun resolveType(name: String): Type {
        return when (name) {
            "int64" -> ScalarType.INT64
            "double" -> ScalarType.DOUBLE
            "boolean" -> ScalarType.BOOLEAN
            else -> {
                symbolResolver.declaredClasses[name]?.let { classDeclaration ->
                    return ClassType(classDeclaration)
                }
                throw CompilationException(UnresolvedTypeError(name))
            }
        }
    }
}