package moklev.generator

import java.io.File
import java.io.PrintWriter

/**
 * @author Moklev Vyacheslav
 */
class ClassTransformerWorker(
        val pathToSources: String,
        val targetPackage: String,
        val transformerPackage: String,
        val transformerClassName: String,
        val interfaceInheritance: Map<String, List<String>>,
        val rootInterfaces: List<String>,
        val rootTypeName: String,
        val rootResultType: String,
        val targetDir: String,
        val classNameTransformer: (String) -> String,
        val interfaceMap: Map<String, String>,
        val newParamName: String,
        val newParamType: String,
        val headerLines: List<String>,
        val transformerHeaderLines: List<String>
) : Worker {
    fun run() {
        val root = File(pathToSources)
        val visitors = mutableMapOf<String, ClassTransformerVisitor>()
        for (file in root.listFiles()) {
            if (!file.isFile)
                continue
            val (tree, stream) = parseFile(file)
            val builder = StringBuilder()
            ClassTransformerVisitor(stream).apply {
                visit(tree)
                if (baseClassName !in interfaceMap)
                    return@apply
                visitors[className] = this
                builder.appendln("package $targetPackage")
                builder.appendln()
                importList.forEach { import ->
                    builder.appendln("import $import")
                }
                for (line in headerLines) {
                    builder.appendln(line)
                }
                builder.appendln()
                val newClassName = classNameTransformer(className)
                builder.append(
                        "class $newClassName<T: $newParamType>(${(listOf(Triple("", newParamName, "T")) + mainConstructorProperties).joinToString { (modifiers, name, type) -> "$modifiers${if (modifiers.isEmpty()) "" else " "}val $name: $type" }}) : ${interfaceMap[baseClassName]} "
                )
                builder.append(body)
                val targetDirWithPackage = File(targetDir + "/" + targetPackage.replace(".", "/"))
                targetDirWithPackage.mkdirs()
                val printWriter = PrintWriter("${targetDirWithPackage.absolutePath}/$newClassName.kt")
                printWriter.write(builder.toString())
                printWriter.close()
            }
        }
        val transformerDir = File(targetDir + "/" + transformerPackage.replace(".", "/"))
        transformerDir.mkdirs()
        val writer = PrintWriter("${transformerDir.absolutePath}/$transformerClassName.kt")
        writer.println("package $transformerPackage")
        writer.println()
        for (line in transformerHeaderLines) {
            writer.println(line)
        }
        writer.println()
        writer.println("class $transformerClassName<T: $newParamType> {")
        for (interfaceName in interfaceMap.keys + listOf("")) {
            writer.println("\tfun annotate$interfaceName(element: ${if (interfaceName.isEmpty()) rootTypeName else interfaceName}, init: T): ${interfaceMap[interfaceName] ?: rootResultType} {")
            for (visitor in visitors.values) {
                if (visitor.baseClassName != interfaceName)
                    continue
                writer.println("\t\tif (element is ${visitor.className})")
                val params = visitor.mainConstructorProperties.joinToString(separator = "") { (_, name, _) -> ", element.$name" }
                writer.println("\t\t\treturn ${classNameTransformer(visitor.className)}(init$params)")
            }
            for (inheritor in interfaceInheritance[interfaceName] ?: (if (interfaceName.isEmpty()) rootInterfaces else listOf())) {
                writer.println("\t\tif (element is $inheritor)")
                writer.println("\t\t\treturn annotate$inheritor(element, init)")
            }
            writer.println("\t\tthrow RuntimeException(\"Unknown semantic element: \$element\")")
            writer.println("\t}")
            writer.println()
        }
        writer.println("}")
        writer.close()
    }
}