package moklev.integrational.evaluation

import moklev.compiler.compilation.SemanticBuilder
import moklev.compiler.compilation.TopLevelContext
import moklev.compiler.evaluation.Evaluator
import moklev.compiler.parsing.ParserUtil
import kotlin.test.assertEquals

/**
 * @author Moklev Vyacheslav
 */
abstract class EvaluationTestBase {
    protected fun runTest(program: String, testExpression: String, expectedResult: String) {
        runMultipleTests(program, listOf(testExpression), listOf(expectedResult))
    }

    protected fun runMultipleTests(program: String, testExpressions: List<String>, expectedResults: List<String>) {
        val programAst = ParserUtil.parse(program)
        val builder = SemanticBuilder()
        builder.build(programAst, TopLevelContext)
        assertEquals(testExpressions.size, expectedResults.size, "Expression and result lists are differ in length")
        for (index in 0 until testExpressions.size) {
            val testExpression = testExpressions[index]
            val expectedResult = expectedResults[index]
            val expressionAst = ParserUtil.parseExpression(testExpression)
            val expression = builder.buildExpression(expressionAst, TopLevelContext)
            val result = Evaluator().evaluateExpression(expression).toString()
            assertEquals(expectedResult, result)
        }
    }
}