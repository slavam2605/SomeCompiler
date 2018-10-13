package moklev.compiler.compilation.analysis

import moklev.compiler.compilation.MonotonicAnalysis
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
interface ExpressionAnalysis<T: MonotonicAnalysis<T>> : StatementAnalysis<T> {
    val type: Type
}