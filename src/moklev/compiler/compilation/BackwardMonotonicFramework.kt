package moklev.compiler.compilation

import moklev.compiler.compilation.analysis.AnalysisAnnotator
import moklev.compiler.compilation.analysis.SomeAnalyzer
import moklev.compiler.compilation.analysis.StatementAnalysis
import moklev.compiler.compilation.analysis.impl.IfAnalysis
import moklev.compiler.compilation.analysis.impl.StatementListAnalysis
import moklev.compiler.compilation.analysis.impl.WhileAnalysis
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.annotation.BasicStatement
import kotlin.reflect.full.findAnnotation

/**
 * @author Moklev Vyacheslav
 */
class BackwardMonotonicFramework<T: MonotonicAnalysis<T>> : SomeAnalyzer<T> {
    fun analyse(root: SemanticStatement, init: T): T {
        val annotatedElement = AnalysisAnnotator<T>().annotateSemanticStatement(root, init)
        val (result, _) = analyseStatement(annotatedElement, init)
        return result
    }
    
    override fun analyseIf(element: IfAnalysis<T>, input: T): Pair<T, Boolean> {
        val (beforeTrue, changedTrue) = analyseStatement(element.bodyTrue, input)
        val (beforeFalse, changedFalse) = analyseStatement(element.bodyFalse, input)
        val beforeIf = beforeTrue.combine(beforeFalse)
        return beforeIf to (change(element, beforeIf) || changedFalse || changedTrue)
    }

    override fun analyseStatementList(element: StatementListAnalysis<T>, input: T): Pair<T, Boolean> {
        var analysis = input
        var changed = false
        for (statement in element.statements.asReversed()) {
            val result = analyseStatement(statement, analysis)
            changed = changed || result.second
            analysis = result.first
        }
        return analysis to (change(element, analysis) || changed)
    }

    override fun analyseWhile(element: WhileAnalysis<T>, input: T): Pair<T, Boolean> {
        var lastBodyOutput = input
        var changed = true
        var onceChanged = false
        while (changed) {
            val result = analyseStatement(element.body, lastBodyOutput)
            changed = result.second
            onceChanged = onceChanged || changed
            lastBodyOutput = result.first
        }
        val beforeWhile = input.combine(lastBodyOutput)
        return beforeWhile to (change(element, beforeWhile) || onceChanged)
    }

    override fun analyseStatement(root: StatementAnalysis<T>, input: T): Pair<T, Boolean> {
        if (root::class.findAnnotation<BasicStatement>() != null) {
            val newValue = input.analyseStatement(root)
            return newValue to change(root, newValue)
        }
        return super.analyseStatement(root, input)
    }
    
    private fun change(element: StatementAnalysis<T>, analysis: T): Boolean {
        val changed = element.lastAnalysis != analysis
        element.lastAnalysis = analysis
        return changed
    }
}