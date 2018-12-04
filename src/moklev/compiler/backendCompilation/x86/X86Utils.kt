package moklev.compiler.backendCompilation.x86

import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

fun sizeLetter(size: Int?) = when (size) {
    1 -> "b"
    2 -> "w"
    4 -> "l"
    8 -> "q"
    else -> throw Exception("Unknown size: $size")
}

fun commonSize(vararg size: Int?): Int {
    val allNotNull = size.filterNotNull()
    if (allNotNull.isEmpty())
        throw Exception("All sized are undefined")
    if (allNotNull.any { it != allNotNull[0] })
        throw Exception("Found different sizes")
    return allNotNull[0]
}

fun machineSizeOf(type: Type): Int = when (type) {
    is ScalarType -> when (type) {
        ScalarType.INT64 -> 8
        ScalarType.BOOLEAN -> 8
        ScalarType.DOUBLE -> 8
    }
    else -> throw Exception("Not supported: $type")
}