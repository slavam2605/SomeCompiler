package moklev.integrational.compilation

import moklev.compiler.compilation.SemanticBuilder
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.parsing.ParserUtil
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * @author Moklev Vyacheslav
 */
abstract class CompilationTestBase {
    protected fun runTestError(program: String, expectedError: String) {
        try {
            buildProgram(program)
        } catch (e: CompilationException) {
            assertEquals(expectedError, e.message)
            println(e.message)
            return
        }
        fail("Expected exception")
    }
    
    protected fun runTestSucceeded(program: String) {
        buildProgram(program)
        println("done")
    }

    private fun buildProgram(program: String) {
        val programAst = ParserUtil.parse(program)
        val builder = SemanticBuilder()
        builder.build(programAst)
    }
}