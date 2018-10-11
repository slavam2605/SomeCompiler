package moklev.compiler.compilation

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
object DiagnosticCompilationErrors {
    val AddressOfError = { target: SemanticExpression -> "Can't get address of $target" }
    val DereferenceTypeError = { type: Type -> "Can dereference only pointer type, found: $type" }
    val InvocationTargetError = { target: SemanticExpression -> "Invocation target is not a function reference: $target" }
    val InvocationWrongNumberOfArgumentsError = { expected: Int, found: Int -> "Wrong number of arguments: expected $expected, found: $found" }
    val InvocationArgumentTypeMismatchError = { index: Int, expected: Type, found: Type -> "Invalid type of argument ${index + 1}, expected: $expected, found: $found" }
    val ReturnTypeMismatchError = { expected: Type, found: Type -> "Function return type mismatch: function should return $expected, actual type: $found" }
    val IfConditionBooleanTypeError = { type: Type -> "Condition of `if` statement should have Boolean type, found: $type" }
    val WhileConditionBooleanTypeError = { type: Type -> "Condition of `while` statement should have Boolean type, found: $type" }
    val AssignTypeMismatchError = { expected: Type, found: Type -> "Type mismatch in assignment: value should have type $expected, found: $found" }
    val NoSuchBinaryOperationError = { op: String, left: Type, right: Type -> "No such overload for binary operator $op and types $left and $right" }
    val FunctionAlreadyDeclaredError = { name: String -> "Function $name is already defined" }
    val VariableAlreadyDeclaredError = { name: String -> "Variable $name is already defined" }
    val UnresolvedSymbolError = { name: String -> "Unresolved symbol: $name" }
    val UnresolvedTypeError = { name: String -> "Unresolved type: $name" }
}