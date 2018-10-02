package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticElement
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class FunctionDeclaration(
        val name: String,
        val parameters: List<Pair<String, Type>>,
        val returnType: Type,
        val body: SemanticStatement
) : SemanticElement