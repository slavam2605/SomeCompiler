package moklev.generator

import moklev.generator.parsing.KotlinParser
import moklev.generator.parsing.KotlinParserBaseVisitor

/**
 * @author Moklev Vyacheslav
 */
class GeneratorVisitor : KotlinParserBaseVisitor<Any?>() {
    var className: String? = null
    var baseClassName: String? = null
    val annotations: MutableList<String> = mutableListOf()

    override fun visitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext) {
        if (className != null)
            throw RuntimeException("More than one class declaration found ($className, ${ctx.simpleIdentifier().text})")
        className = ctx.simpleIdentifier().text
        annotations.addAll(
                ctx.modifierList()?.annotations()?.mapNotNull { it.annotation()?.text?.trim()?.removePrefix("@") } ?: listOf()
        )
        for (specifier in ctx.delegationSpecifiers()?.delegationSpecifier() ?: emptyList()) {
            if (specifier.userType() == null)
                continue
            if (baseClassName != null)
                throw RuntimeException("More than one base class or interface found for class: $className")
            baseClassName = specifier.userType().text
        }
    }
}