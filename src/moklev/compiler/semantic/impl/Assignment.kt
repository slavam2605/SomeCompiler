package moklev.compiler.semantic.impl

import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement

/**
 * @author Moklev Vyacheslav
 */
class Assignment(val target: SemanticExpression, val value: SemanticExpression) : SemanticStatement