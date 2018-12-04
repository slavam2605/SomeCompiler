package moklev.integrational.evaluation

import moklev.compiler.backendCompilation.x86.X86BackendCompiler
import moklev.compiler.backendCompilation.x86.targetSpecific.WindowsSpecificTool
import moklev.compiler.compilation.SemanticBuilder
import moklev.compiler.compilation.TopLevelContext
import moklev.compiler.evaluation.Evaluator
import moklev.compiler.parsing.ParserUtil
import moklev.compiler.semantic.impl.FunctionDeclaration
import moklev.compiler.semantic.impl.Return
import moklev.compiler.types.ScalarType
import org.junit.runner.RunWith
import java.io.File
import java.io.PrintWriter
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * @author Moklev Vyacheslav
 */
@RunWith(EvaluationTestRunner::class)
abstract class EvaluationTestBase(val testMode: EvaluationTestMode) {
    private val x86ToolsFolder = "x86_test_tools"

    protected fun runTest(program: String, testExpression: String, expectedResult: String) {
        runMultipleTests(program, listOf(testExpression), listOf(expectedResult))
    }

    protected fun runMultipleTests(program: String, testExpressions: List<String>, expectedResults: List<String>) {
        val programAst = ParserUtil.parse(program)
        val builder = SemanticBuilder()
        builder.build(programAst, TopLevelContext)
        assertEquals(testExpressions.size, expectedResults.size, "Expression and result lists are differ in length")
        when (testMode) {
            EvaluationTestMode.Evaluation -> {
                for (index in 0 until testExpressions.size) {
                    val testExpression = testExpressions[index]
                    val expectedResult = expectedResults[index]
                    val expressionAst = ParserUtil.parseExpression(testExpression)
                    val expression = builder.buildExpression(expressionAst, TopLevelContext)
                    val result = Evaluator().evaluateExpression(expression).toString()
                    assertEquals(expectedResult, result)
                }
            }
            EvaluationTestMode.X86Compilation -> {
                val singleExpression = testExpressions.single()
                val singleExpectedResult = expectedResults.single()
                val expression = ParserUtil.parseExpression(singleExpression).let {
                    builder.buildExpression(it, TopLevelContext)
                }
                val testFunction = FunctionDeclaration("test", emptyList(), expression.type)
                builder.symbolResolver.declareFunction(testFunction)
                testFunction.complete(Return(expression))
                val compiler = X86BackendCompiler(WindowsSpecificTool)
                for ((_, functionDeclaration) in builder.symbolResolver.declaredFunctions) {
                    compiler.compileFunctionDeclaration(functionDeclaration, TopLevelContext)
                }
                PrintWriter(File(x86ToolsFolder, "out.s")).use { writer ->
                    for (line in compiler.builder.lines) {
                        writer.println(line)
                    }
                }
                PrintWriter(File(x86ToolsFolder, "main.c")).use { writer ->
                    val (definition, call) = when (expression.type) {
                        ScalarType.INT64 -> "int64_t test();" to "format_int64(test());"
                        ScalarType.BOOLEAN -> "bool test();" to "format_bool(test());"
                        else -> throw Exception("Unsupported type: ${expression.type}")
                    }
                    val template = File(x86ToolsFolder, "main_template.c").bufferedReader().readText()
                    val source = template
                            .replace("\$test_definition", definition)
                            .replace("\$test_call", call)
                    writer.write(source)
                }
                ProcessBuilder("gcc", "main.c", "out.s", "-o", "main.exe")
                        .directory(File(x86ToolsFolder))
                        .start().let { process ->
                            if (process.waitFor() != 0) {
                                System.err.println(process.errorStream.bufferedReader().readText())
                                fail("gcc exited with code ${process.exitValue()}")
                            } else {
                                println("gcc exited normally")
                            }
                        }
                val result = ProcessBuilder("$x86ToolsFolder/main.exe")
                        .directory(File(x86ToolsFolder))
                        .start().let { process ->
                    assertEquals(0, process.waitFor(), "Nonzero return code")
                    process.inputStream.bufferedReader().readText()
                }
                println("Result: $result")
                assertEquals(singleExpectedResult, result)
            }
        }
    }
}