package moklev.compiler.compilation.analysis

import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
interface ExpressionAnalysis : StatementAnalysis {
    val type: Type
}