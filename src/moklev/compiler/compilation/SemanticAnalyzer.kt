package moklev.compiler.compilation

import moklev.compiler.compilation.analyzers.NoMissingReturnAnalyzer
import moklev.compiler.semantic.impl.FunctionDeclaration

/**
 * @author Moklev Vyacheslav
 */
class SemanticAnalyzer {
    fun analyzeFunctionDeclaration(declaration: FunctionDeclaration) {
        NoMissingReturnAnalyzer.analyse(declaration)
    }
}