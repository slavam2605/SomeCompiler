package moklev.compiler.ast.impl

import moklev.compiler.ast.ASTNode
import moklev.compiler.ast.DeclarationASTNode

/**
 * @author Moklev Vyacheslav
 */
class DeclarationListNode(val declarations: List<DeclarationASTNode>) : ASTNode