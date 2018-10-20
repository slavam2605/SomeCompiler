package moklev.compiler.compilation.analyzers

import moklev.compiler.compilation.BackwardMonotonicFramework
import moklev.compiler.compilation.DiagnosticCompilationErrors.MissingReturnError
import moklev.compiler.compilation.MonotonicAnalysis
import moklev.compiler.compilation.analysis.impl.AssignmentAnalysis
import moklev.compiler.compilation.analysis.impl.ReturnAnalysis
import moklev.compiler.compilation.analysis.impl.VariableDeclarationAnalysis
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.impl.FunctionDeclaration

/**
 * @author Moklev Vyacheslav
 */
object NoMissingReturnAnalyzer {
    fun analyse(declaration: FunctionDeclaration) {
        val result = BackwardMonotonicFramework<NoMissingReturnAnalysis>()
                .analyse(declaration.body, NoMissingReturnAnalysis(false))
        if (!result.value)
            throw CompilationException(declaration, MissingReturnError(declaration.name))
    }
    
    @Suppress("EqualsOrHashCode")
    private class NoMissingReturnAnalysis(val value: Boolean) : MonotonicAnalysis<NoMissingReturnAnalysis> {
        override fun combine(other: NoMissingReturnAnalysis) = 
                NoMissingReturnAnalysis(value && other.value)

        override fun analyseAssignment(element: AssignmentAnalysis<NoMissingReturnAnalysis>) =
                this

        override fun analyseReturn(element: ReturnAnalysis<NoMissingReturnAnalysis>) =
                NoMissingReturnAnalysis(true)

        override fun analyseVariableDeclaration(element: VariableDeclarationAnalysis<NoMissingReturnAnalysis>) =
                this

        override fun equals(other: Any?) = (other as? NoMissingReturnAnalysis)?.value == value
    }
}