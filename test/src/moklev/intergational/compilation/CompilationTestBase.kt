package moklev.intergational.compilation

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
            val programAst = ParserUtil.parse(program)
            val builder = SemanticBuilder()
            builder.build(programAst)
        } catch (e: CompilationException) {
            assertEquals(expectedError, e.message)
            return
        }
        fail("Expected exception")
    }
}