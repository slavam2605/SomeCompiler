package moklev.compiler.compilation.analysis

import moklev.compiler.compilation.MonotonicAnalysis

/**
 * @author Moklev Vyacheslav
 */
interface StatementAnalysis<T: MonotonicAnalysis<T>> {
    var lastAnalysis: T
}