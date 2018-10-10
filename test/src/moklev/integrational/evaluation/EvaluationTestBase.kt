package moklev.integrational.evaluation

import moklev.compiler.compilation.SemanticBuilder
import moklev.compiler.evaluation.Evaluator
import moklev.compiler.parsing.ParserUtil
import kotlin.test.assertEquals

/**
 * @author Moklev Vyacheslav
 */
abstract class EvaluationTestBase {
    protected fun runTest(program: String, testExpression: String, expectedResult: String) {
        val programAst = ParserUtil.parse(program)
        val expressionAst = ParserUtil.parseExpression(testExpression)
        val builder = SemanticBuilder()
        builder.build(programAst)
        val expression = builder.buildExpression(expressionAst)
        val result = Evaluator().evaluateExpression(expression).toString()
        assertEquals(expectedResult, result)
    }
}