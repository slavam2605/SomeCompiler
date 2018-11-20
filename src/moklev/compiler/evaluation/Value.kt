package moklev.compiler.evaluation

import moklev.compiler.types.*

/**
 * @author Moklev Vyacheslav
 */
sealed class Value {
    class Int64(val value: kotlin.Long) : Value()
    class Double(val value: kotlin.Double) : Value()
    class Boolean(val value: kotlin.Boolean) : Value()
    abstract class Pointer : Value() {
        abstract val sourceType: Type
        abstract fun write(value: Value)
        abstract fun read(): Value
    }
    abstract class ArrayPointer : Pointer() {
        abstract fun shift(count: Long): ArrayPointer
    }
    class Class(override val type: ClassType): Value() {
        val fields = mutableMapOf<String, Value>()
    }
    
    val int64Value: kotlin.Long
        get() = (this as Int64).value
    
    val doubleValue: kotlin.Double
        get() = (this as Double).value
    
    val booleanValue: kotlin.Boolean
        get() = (this as Boolean).value

    open val type
        get() = when (this) {
            is Int64 -> ScalarType.INT64
            is Double -> ScalarType.DOUBLE
            is Boolean -> ScalarType.BOOLEAN
            is ArrayPointer -> ArrayPointerType(sourceType)
            is Pointer -> PointerType(sourceType)
            is Class -> error("Overridden in subclass")
        }
    
    override fun toString(): String {
        return when (this) {
            is Int64 -> "Int64[$value]"
            is Double -> "Double[$value]"
            is Boolean -> "Boolean[$value]"
            is ArrayPointer -> "ArrayPointer[?]"
            is Pointer -> "Pointer[?]"
            is Class -> "Class<${type.declaration.name}>{${type.declaration.fields.map { fields[it.name] }.joinToString()}}"
        }
    }
}