package moklev.compiler.compilation

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class TypeResolver {
    fun resolveType(name: String): Type {
        return when (name) {
            "int64" -> ScalarType.INT64
            "double" -> ScalarType.DOUBLE
            "boolean" -> ScalarType.BOOLEAN
            else -> throw CompilationException("Unknown type: \"$name\"")
        }
    }
}