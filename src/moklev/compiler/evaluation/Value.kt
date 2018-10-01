package moklev.compiler.evaluation

import moklev.compiler.types.ScalarType

/**
 * @author Moklev Vyacheslav
 */
sealed class Value {
    class Int64(val value: kotlin.Long) : Value()
    class Double(val value: kotlin.Double) : Value()
    class Boolean(val value: kotlin.Boolean) : Value()
    
    val int64Value: kotlin.Long
        get() = (this as Int64).value
    
    val doubleValue: kotlin.Double
        get() = (this as Double).value
    
    val booleanValue: kotlin.Boolean
        get() = (this as Boolean).value

    val type
        get() = when (this) {
            is Int64 -> ScalarType.INT64
            is Double -> ScalarType.DOUBLE
            is Boolean -> ScalarType.BOOLEAN
        }
    
    override fun toString(): String {
        return when (this) {
            is Int64 -> "Int64[$value]"
            is Double -> "Double[$value]"
            is Boolean -> "Boolean[$value]" 
        }
    }
}