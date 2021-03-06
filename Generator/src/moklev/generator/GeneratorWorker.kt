package moklev.generator

import java.io.File
import java.io.PrintWriter

/**
 * @author Moklev Vyacheslav
 */
class GeneratorWorker(
        val pathToSources: String,
        val pathToSourcesImpl: String,
        val trimInterface: (String?) -> String,
        val trimImpl: (String?) -> String,
        val withTypeParameter: Boolean,
        val typeParameterBound: String,
        val methodPrefix: String,
        val methodParamName: String,
        val additionalParamsMap: (String) -> List<String>,
        val exceptionName: String,
        val passElementToException: Boolean,
        val interfaceMap: Map<String, String>,
        val ignoreInterfaces: Set<String>,
        val isAvailableAnnotation: (List<String>) -> Boolean,
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
        val implRoot = File(pathToSourcesImpl)
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
                if (!isAvailableAnnotation(annotations))
                    return@apply
                if (baseClassName in ignoreInterfaces)
                    return@apply
                if (baseClassName == null) {
                    System.err.println("No parsed base class for `$className`")
                    return@apply
                }
                val trimmedClassName = trimImpl(className)
                val returnTypeString = interfaceMap[baseClassName!!]?.let { ": $it" } ?: ""
                val typeParamString = if (withTypeParameter) "<T>" else ""
                val additionalParams = additionalParamsMap(baseClassName!!)
                builder.appendln("\tfun $methodPrefix$trimmedClassName($methodParamName: $className$typeParamString${additionalParams.joinToString(separator = "") { ", $it" }})$returnTypeString")
                builder.appendln()
                interfaceImplementations.compute(baseClassName!!) { _, list ->
                    list?.apply { add(className!!) } ?: mutableListOf(className!!)
                }
            }
        }
        for ((interfaceName, implementations) in interfaceImplementations) {
            val trimmedInterfaceName = trimInterface(interfaceName)
            val returnTypeString = interfaceMap[interfaceName]?.let { ": $it" } ?: ""
            val additionalParams = additionalParamsMap(interfaceName)
            builder.appendln("\tfun $methodPrefix$trimmedInterfaceName(root: $interfaceName${additionalParams.joinToString(separator = "") { ", $it" }})$returnTypeString {")
            for (implementation in implementations) {
                val trimmedImplName = trimImpl(implementation)
                val typeParamString = if (withTypeParameter) "<*>" else ""
                val typeParamCast = if (withTypeParameter) " as $implementation<T>" else ""
                builder.appendln("\t\tif (root is $implementation$typeParamString)")
                builder.appendln("\t\t\treturn $methodPrefix$trimmedImplName(root$typeParamCast${additionalParams.joinToString(separator = "") { ", ${it.split(":")[0]}" }})")
            }
            if (interfaceName !in onlyImplInterfaces) {
                for (inheritor in interfaceInheritors[interfaceName] ?: emptyList<String>()) {
                    val trimmedInheritor = trimInterface(inheritor)
                    val typeEliminator = if (interfaceName !in interfaceMap && inheritor in interfaceMap)
                        ".let { Unit }"
                    else
                        ""
                    builder.appendln("\t\tif (root is $inheritor)")
                    builder.appendln("\t\t\treturn $methodPrefix$trimmedInheritor(root${additionalParams.joinToString(separator = "") { ", ${it.split(":")[0]}" }})$typeEliminator")
                }
            }
            builder.appendln("\t\tthrow $exceptionName(${if (passElementToException) "root, " else ""}\"Unknown $interfaceName: \$root\")")
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
        val typeParamString = if (withTypeParameter) "<T: $typeParameterBound>" else ""
        printWriter.println("interface $generatedClassName$typeParamString {")
        printWriter.print(builder.toString())
        printWriter.print("}")
        printWriter.close()
    }
}