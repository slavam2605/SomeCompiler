package moklev.generator

import moklev.generator.parsing.KotlinLexer
import moklev.generator.parsing.KotlinParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import java.io.File
import java.io.PrintWriter

/**
 * @author Moklev Vyacheslav
 */
class GeneratorWorker(
        val pathToSources: String,
        val trimInterface: (String?) -> String,
        val trimImpl: (String?) -> String,
        val methodPrefix: String,
        val methodParamName: String,
        val exceptionName: String,
        val interfaceMap: Map<String, String>,
        val onlyImplInterfaces: Set<String>,
        val targetPackage: String,
        val targetDir: String,
        val headerLines: List<String>,
        val generatedClassName: String
) : Worker {
    var rootInterface: String? = null
    val interfaceInheritors = mutableMapOf<String, MutableList<String>>()
    val interfaceImplementations = mutableMapOf<String, MutableList<String>>()
    
    fun run() {
        val root = File(pathToSources)
        val implRoot = File("$pathToSources/impl")
        for (interfaceFile in root.listFiles()) {
            if (!interfaceFile.isFile)
                continue
            val (tree, _) = parseFile(interfaceFile)
            GeneratorVisitor().apply {
                visit(tree)
                if (baseClassName == null)
                    rootInterface = className
                else 
                    interfaceInheritors.compute(baseClassName!!) { _, list ->
                        list?.apply { add(className!!) } ?: mutableListOf(className!!)
                    }
            }
        }
        val builder = StringBuilder()
        for (implFile in implRoot.listFiles()) {
            if (!implFile.isFile)
                continue
            val (tree, _) = parseFile(implFile)
            GeneratorVisitor().apply { 
                visit(tree)
                val trimmedClassName = trimImpl(className)
                val returnTypeString = interfaceMap[baseClassName!!]?.let { ": $it" } ?: ""
                builder.appendln("\tfun $methodPrefix$trimmedClassName($methodParamName: $className)$returnTypeString")
                builder.appendln()
                interfaceImplementations.compute(baseClassName!!) { _, list ->
                    list?.apply { add(className!!) } ?: mutableListOf(className!!)
                }
            }
        }
        for ((interfaceName, implementations) in interfaceImplementations) {
            val trimmedInterfaceName = trimInterface(interfaceName)
            val returnTypeString = interfaceMap[interfaceName]?.let { ": $it" } ?: ""
            builder.appendln("\tfun $methodPrefix$trimmedInterfaceName(root: $interfaceName)$returnTypeString {")
            for (implementation in implementations) {
                val trimmedImplName = trimImpl(implementation)
                builder.appendln("\t\tif (root is $implementation)")
                builder.appendln("\t\t\treturn $methodPrefix$trimmedImplName(root)")
            }
            if (interfaceName !in onlyImplInterfaces) {
                for (inheritor in interfaceInheritors[interfaceName] ?: emptyList<String>()) {
                    val trimmedInheritor = trimInterface(inheritor)
                    val typeEliminator = if (interfaceName !in interfaceMap && inheritor in interfaceMap)
                        ".let { Unit }"
                    else
                        ""
                    builder.appendln("\t\tif (root is $inheritor)")
                    builder.appendln("\t\t\treturn $methodPrefix$trimmedInheritor(root)$typeEliminator")
                }
            }
            builder.appendln("\t\tthrow $exceptionName(root, \"Unknown $interfaceName: \$root\")")
            builder.appendln("\t}")
            builder.appendln()
        }
        builder.delete(builder.length - System.lineSeparator().length, builder.length)
        
        val targetDirWithPackage = File(targetDir + "/" + targetPackage.replace(".", "/"))
        targetDirWithPackage.mkdirs()
        val printWriter = PrintWriter("${targetDirWithPackage.absolutePath}/$generatedClassName.kt")
        printWriter.println("package $targetPackage")
        printWriter.println()
        for (headerLine in headerLines) {
            printWriter.println(headerLine)
        }
        printWriter.println()
        printWriter.println("interface $generatedClassName {")
        printWriter.print(builder.toString())
        printWriter.print("}")
        printWriter.close()
    }
}