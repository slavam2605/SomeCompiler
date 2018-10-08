package moklev.generator

/**
 * @author Moklev Vyacheslav
 */
fun main(args: Array<String>) {
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/ast",
            trimInterface = { it!!.removeSuffix("ASTNode") },
            trimImpl = { it!!.removeSuffix("Node") },
            methodPrefix = "build",
            methodParamName = "node",
            exceptionName = "CompilationException",
            interfaceMap = mapOf(
                    "StatementASTNode" to "SemanticStatement",
                    "ExpressionASTNode" to "SemanticExpression",
                    "TypeASTNode" to "Type"
            ),
            onlyImplInterfaces = setOf("ASTNode"),
            targetPackage = "moklev.compiler.compilation",
            targetDir = "gen",
            headerLines = listOf(
                    "import moklev.compiler.ast.*",
                    "import moklev.compiler.ast.impl.*",
                    "import moklev.compiler.semantic.*",
                    "import moklev.compiler.types.*",
                    "import moklev.compiler.exceptions.CompilationException"
            ),
            generatedClassName = "SomeBuilder"
    ).run()
    
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/semantic",
            trimInterface = { it!!.removePrefix("Semantic") },
            trimImpl = { it!! },
            methodPrefix = "evaluate",
            methodParamName = "element",
            exceptionName = "EvaluationException",
            interfaceMap = mapOf("SemanticExpression" to "Value"),
            onlyImplInterfaces = setOf(),
            targetPackage = "moklev.compiler.evaluation",
            targetDir = "gen",
            headerLines = listOf(
                    "import moklev.compiler.exceptions.EvaluationException",
                    "import moklev.compiler.semantic.*",
                    "import moklev.compiler.semantic.impl.*"
            ),
            generatedClassName = "SomeEvaluator"
    ).run()
}