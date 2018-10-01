package moklev.compiler.evaluation

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticElement
import moklev.compiler.semantic.impl.DoubleConstant
import moklev.compiler.semantic.impl.Int64BinaryOperation
import moklev.compiler.semantic.impl.Int64Constant

/**
 * @author Moklev Vyacheslav
 */
class Evaluator {
    fun evaluate(element: SemanticElement): Value {
        if (element is Int64Constant)
            return evaluateInt64Constant(element)
        if (element is DoubleConstant)
            return evaluateDoubleConstant(element)
        if (element is Int64BinaryOperation)
            return evaluateInt64BinaryOperation(element)
        throw CompilationException(element, "Unknown semantic element: $element")
    }
    
    fun evaluateInt64Constant(element: Int64Constant): Value {
        return Value.Int64(element.value)
    }
    
    fun evaluateDoubleConstant(element: DoubleConstant): Value {
        return Value.Double(element.value)
    }
    
    fun evaluateInt64BinaryOperation(element: Int64BinaryOperation): Value {
        val left = evaluate(element.left)
        val right = evaluate(element.right)
        return when (element.op) {
            "+" -> Value.Int64(left.int64Value + right.int64Value)
            "==" -> Value.Boolean(left.int64Value == right.int64Value)
            else -> throw CompilationException(element, "Unknown op for Int64BinaryOperation: \"${element.op}\"")
        }
    }
}