package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement

/**
 * @author Moklev Vyacheslav
 */
class Return(val value: SemanticExpression) : SemanticStatement