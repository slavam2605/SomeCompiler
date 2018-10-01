package moklev.compiler.ast.impl

import moklev.compiler.ast.ASTNode

/**
 * @author Moklev Vyacheslav
 */
class BinaryOperation(val op: String, val left: ASTNode, val right: ASTNode) : ASTNode