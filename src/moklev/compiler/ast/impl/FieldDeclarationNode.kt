package moklev.compiler.ast.impl

import moklev.compiler.ast.DeclarationASTNode
import moklev.compiler.ast.TypeASTNode

/**
 * @author Moklev Vyacheslav
 */
class FieldDeclarationNode(val name: String, val type: TypeASTNode) : DeclarationASTNode