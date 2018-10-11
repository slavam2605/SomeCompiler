package moklev.compiler.evaluation

import moklev.compiler.exceptions.EvaluationException
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
object Predefined {
    fun invokeFunction(name: String, parameters: List<Value>): Value {
        return when (name) {
            "createInt64Array" -> createInt64Array(parameters[0])
            "debugPrint" -> parameters.joinToString().let { println(it); Value.Int64(0) }
            else -> throw EvaluationException("Unknown predefined function: $name")
        }
    }
    
    private fun createInt64Array(n: Value): Value {
        return ArrayPointerToInt64Array(LongArray(n.int64Value.toInt()) { 0 }, 0)
    }

    private class ArrayPointerToInt64Array(val array: LongArray, val index: Long): Value.ArrayPointer() {
        override val sourceType: Type = ScalarType.INT64

        override fun write(value: Value) {
            array[index.toInt()] = value.int64Value
        }

        override fun read(): Value {
            return Value.Int64(array[index.toInt()])
        }

        override fun shift(count: Long): ArrayPointer {
            return ArrayPointerToInt64Array(array, index + count)
        }

        override fun toString(): String {
            return "ArrayPointer[$index, ${array.contentToString()}]"
        }
    }
}