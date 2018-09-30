package moklev.compiler.compilation

import moklev.compiler.ast.ASTNode
import moklev.compiler.ast.SemanticASTNode
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticElement

/**
 * @author Moklev Vyacheslav
 */
class SemanticBuilder {
    fun build(root: ASTNode): SemanticElement {
        if (root is SemanticASTNode)
            return root.toSemanticElement()
        throw CompilationException(root, "`root` is not a SemanticASTNode")
    }
}