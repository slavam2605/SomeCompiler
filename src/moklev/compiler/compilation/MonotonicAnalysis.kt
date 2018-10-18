package moklev.compiler.compilation

import moklev.compiler.compilation.analysis.SomeBasicAnalyzer

/**
 * @author Moklev Vyacheslav
 */
interface MonotonicAnalysis<T: MonotonicAnalysis<T>> : SomeBasicAnalyzer<T> {
    fun combine(other: T): T
}