package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement

/**
 * @author Moklev Vyacheslav
 */
class If(
        val condition: SemanticExpression,
        val bodyTrue: SemanticStatement,
        val bodyFalse: SemanticStatement
) : SemanticStatement