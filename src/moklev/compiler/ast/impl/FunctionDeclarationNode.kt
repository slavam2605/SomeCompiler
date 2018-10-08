package moklev.compiler.ast.impl

import moklev.compiler.ast.DeclarationASTNode
import moklev.compiler.ast.StatementASTNode
import moklev.compiler.ast.TypeASTNode

/**
 * @author Moklev Vyacheslav
 */
class FunctionDeclarationNode(
        val name: String,
        val parameters: List<Pair<String, TypeASTNode>>,
        val returnType: TypeASTNode,
        val body: StatementASTNode
) : DeclarationASTNode