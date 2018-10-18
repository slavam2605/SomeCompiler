package moklev.generator

import moklev.generator.parsing.KotlinParser
import moklev.generator.parsing.KotlinParserBaseVisitor
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.misc.Interval

/**
 * @author Moklev Vyacheslav
 */
class ClassTransformerVisitor(val stream: CharStream) : KotlinParserBaseVisitor<Any?>() {
    lateinit var className: String
    lateinit var baseClassName: String
    var body: String = ""
    val importList = mutableListOf<String>()
    val mainConstructorProperties = mutableListOf<Triple<String, String, String>>()
    val annotations: MutableList<String> = mutableListOf()

    override fun visitImportHeader(ctx: KotlinParser.ImportHeaderContext) {
        importList.add(
                if (ctx.DOT() == null) {
                    ctx.identifier().text
                } else {
                    "${ctx.identifier().text}.*"
                }
        )
    }

    override fun visitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext) {
        if (this::className.isInitialized)
            throw RuntimeException("More than one class declaration found ($className, ${ctx.simpleIdentifier().text})")
        className = ctx.simpleIdentifier().text
        annotations.addAll(
                ctx.modifierList()?.annotations()?.mapNotNull { it.annotation()?.text?.trim()?.removePrefix("@") } ?: listOf()
        )
        for (specifier in ctx.delegationSpecifiers()?.delegationSpecifier() ?: emptyList()) {
            if (specifier.userType() == null)
                continue
            if (this::baseClassName.isInitialized)
                throw RuntimeException("More than one base class or interface found for class: $className")
            baseClassName = specifier.userType().text
        }
        if (ctx.classBody() != null) {
            val classBody = ctx.classBody()
            body = stream.getText(Interval(classBody.start.startIndex, classBody.stop.stopIndex))
        }
        visitPrimaryConstructor(ctx.primaryConstructor())
    }

    override fun visitPrimaryConstructor(ctx: KotlinParser.PrimaryConstructorContext) {
        ctx.classParameters().classParameter().forEach(::visitClassParameter)
    }

    override fun visitClassParameter(ctx: KotlinParser.ClassParameterContext) {
        val modifiers = ctx.modifierList()?.let { stream.getText(Interval(it.start.startIndex, it.stop.stopIndex)) } ?: ""
        mainConstructorProperties.add(Triple(modifiers, ctx.simpleIdentifier().text, ctx.type().text))
    }
}