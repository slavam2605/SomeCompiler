package moklev.compiler.ast.impl

import moklev.compiler.ast.DeclarationASTNode

/**
 * @author Moklev Vyacheslav
 */
class ClassDeclarationNode(val name: String, val innerDeclarations: List<DeclarationASTNode>) : DeclarationASTNode