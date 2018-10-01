package moklev.compiler.evaluation

import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.impl.DoubleConstant
import moklev.compiler.semantic.impl.Int64BinaryOperation
import moklev.compiler.semantic.impl.Int64Constant

/**
 * @author Moklev Vyacheslav
 */
class Evaluator {
    fun evaluateExpression(expression: SemanticExpression): Value {
        if (expression is Int64Constant)
            return evaluateInt64Constant(expression)
        if (expression is DoubleConstant)
            return evaluateDoubleConstant(expression)
        if (expression is Int64BinaryOperation)
            return evaluateInt64BinaryOperation(expression)
        throw CompilationException(expression, "Unknown semantic expression: $expression")
    }
    
    fun evaluateInt64Constant(element: Int64Constant): Value {
        return Value.Int64(element.value)
    }
    
    fun evaluateDoubleConstant(element: DoubleConstant): Value {
        return Value.Double(element.value)
    }
    
    fun evaluateInt64BinaryOperation(element: Int64BinaryOperation): Value {
        val left = evaluateExpression(element.left)
        val right = evaluateExpression(element.right)
        return when (element.op) {
            "+" -> Value.Int64(left.int64Value + right.int64Value)
            "==" -> Value.Boolean(left.int64Value == right.int64Value)
            else -> throw CompilationException(element, "Unknown op for Int64BinaryOperation: \"${element.op}\"")
        }
    }
}