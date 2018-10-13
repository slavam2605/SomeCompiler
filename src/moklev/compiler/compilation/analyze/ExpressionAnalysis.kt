package moklev.compiler.compilation.analyze

import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
interface ExpressionAnalysis : StatementAnalysis {
    val type: Type
}