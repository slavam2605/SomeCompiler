package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticDeclaration

/**
 * @author Moklev Vyacheslav
 */
class ClassDeclaration(
        val name: String, 
        val fields: List<FieldDeclaration>, 
        val methods: List<FunctionDeclaration>
) : SemanticDeclaration