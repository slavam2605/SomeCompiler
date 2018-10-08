package moklev.compiler.ast.impl

import moklev.compiler.ast.StatementASTNode
import moklev.compiler.ast.TypeASTNode

/**
 * @author Moklev Vyacheslav
 */
class VariableDeclarationNode(val name: String, val type: TypeASTNode) : StatementASTNode