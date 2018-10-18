package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.annotation.BasicStatement

/**
 * @author Moklev Vyacheslav
 */
@BasicStatement
class Return(val value: SemanticExpression) : SemanticStatement