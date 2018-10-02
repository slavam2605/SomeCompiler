package moklev.compiler.exceptions

import moklev.compiler.evaluation.Value

/**
 * @author Moklev Vyacheslav
 */
class ReturnException(val value: Value) : ControlFlowException()