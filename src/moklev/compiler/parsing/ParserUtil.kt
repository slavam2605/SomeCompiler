package moklev.compiler.parsing

import moklev.compiler.ast.ASTNode
import moklev.compiler.ast.ExpressionASTNode
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

/**
 * @author Moklev Vyacheslav
 */
object ParserUtil {
    fun parse(contents: String): ASTNode {
        val stream = CharStreams.fromString(contents)
        val lexer = SomeLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = SomeParser(tokenStream)
        return parser.file().result
    }
    
    fun parseExpression(contents: String): ExpressionASTNode {
        val stream = CharStreams.fromString(contents)
        val lexer = SomeLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = SomeParser(tokenStream)
        return parser.expression().result
    }
}