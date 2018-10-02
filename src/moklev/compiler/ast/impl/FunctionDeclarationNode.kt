package moklev.compiler.ast.impl

import moklev.compiler.ast.DeclarationASTNode
import moklev.compiler.ast.StatementASTNode

/**
 * @author Moklev Vyacheslav
 */
class FunctionDeclarationNode(
        val name: String,
        val parameters: List<Pair<String, String>>,
        val returnType: String,
        val body: StatementASTNode
) : DeclarationASTNode