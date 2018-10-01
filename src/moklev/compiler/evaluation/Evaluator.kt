package moklev.compiler.evaluation

import moklev.compiler.exceptions.EvaluationException
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class Evaluator {
    val variableType = mutableMapOf<String, Type>()
    val variableState = mutableMapOf<String, Value>()
    
    fun evaluateStatement(statement: SemanticStatement) {
        if (statement is Assignment)
            return evaluateAssignment(statement)
        if (statement is StatementList)
            return evaluateStatementList(statement)
        if (statement is VariableDeclaration)
            return evaluateVariableDeclaration(statement)
        if (statement is SemanticExpression)
            return evaluateExpression(statement).let { Unit }
        throw EvaluationException(statement, "Unknown semantic statement: $statement")
    }
    
    fun evaluateExpression(expression: SemanticExpression): Value {
        if (expression is Int64Constant)
            return evaluateInt64Constant(expression)
        if (expression is DoubleConstant)
            return evaluateDoubleConstant(expression)
        if (expression is Int64BinaryOperation)
            return evaluateInt64BinaryOperation(expression)
        if (expression is VariableReference)
            return evaluateVariableReference(expression)
        throw EvaluationException(expression, "Unknown semantic expression: $expression")
    }
    
    fun evaluateVariableReference(element: VariableReference): Value {
        return variableState[element.name]
                ?: throw EvaluationException(element, "Variable is not initialized: ${element.name}") 
    }
    
    fun evaluateAssignment(element: Assignment) {
        val value = evaluateExpression(element.value)
        val type = variableType.get(element.variableName) 
                ?: throw EvaluationException(element, "Variable ${element.variableName} is undefined")
        if (value.type != type)
            throw EvaluationException(element, "Type mismatch: ${value.type} and $type")
        variableState[element.variableName] = value
    }
    
    fun evaluateStatementList(element: StatementList) {
        element.statements.forEach { statement ->
            evaluateStatement(statement)
        }
    }
    
    fun evaluateVariableDeclaration(element: VariableDeclaration) {
        variableType[element.name] = element.type
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
            else -> throw EvaluationException(element, "Unknown op for Int64BinaryOperation: \"${element.op}\"")
        }
    }
}