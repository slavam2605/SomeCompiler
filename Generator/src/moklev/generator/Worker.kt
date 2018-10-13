package moklev.generator

import moklev.generator.parsing.KotlinLexer
import moklev.generator.parsing.KotlinParser
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import java.io.File

/**
 * @author Moklev Vyacheslav
 */
interface Worker {
    fun parseFile(file: File): Pair<ParseTree, CharStream> {
        val stream = CharStreams.fromFileName(file.absolutePath)
        val kotlinLexer = KotlinLexer(stream)
        val tokenStream = CommonTokenStream(kotlinLexer)
        val kotlinParser = KotlinParser(tokenStream)
        return kotlinParser.kotlinFile() to stream
    }
}