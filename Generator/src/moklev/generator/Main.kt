package moklev.generator

/**
 * @author Moklev Vyacheslav
 */
fun main(args: Array<String>) {
    println("Generating visitor for AST nodes...")
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/ast",
            pathToSourcesImpl = "src/moklev/compiler/ast/impl",
            trimInterface = { it!!.removeSuffix("ASTNode") },
            trimImpl = { it!!.removeSuffix("Node") },
            withTypeParameter = false,
            typeParameterBound = "",
            methodPrefix = "build",
            methodParamName = "node",
            additionalParamsMap = {
                when (it) {
                    "DeclarationASTNode" -> listOf("context: CompilationContext")
                    else -> listOf()
                }
            },
            exceptionName = "CompilationException",
            passElementToException = true,
            interfaceMap = mapOf(
                    "StatementASTNode" to "SemanticStatement",
                    "ExpressionASTNode" to "SemanticExpression",
                    "TypeASTNode" to "Type"
            ),
            ignoreInterfaces = setOf(),
            isAvailableAnnotation = { true },
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

    println("Generating evaluator for semantic elements...")
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/semantic",
            pathToSourcesImpl = "src/moklev/compiler/semantic/impl",
            trimInterface = { it!!.removePrefix("Semantic") },
            trimImpl = { it!! },
            withTypeParameter = false,
            typeParameterBound = "",
            methodPrefix = "evaluate",
            methodParamName = "element",
            additionalParamsMap = { listOf() },
            exceptionName = "EvaluationException",
            passElementToException = true,
            interfaceMap = mapOf("SemanticExpression" to "Value"),
            ignoreInterfaces = setOf(),
            isAvailableAnnotation = { true },
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

    println("Generating annotated semantic elements with analyses...")
    ClassTransformerWorker(
            pathToSources = "src/moklev/compiler/semantic/impl",
            targetPackage = "moklev.compiler.compilation.analysis.impl",
            transformerPackage = "moklev.compiler.compilation.analysis",
            transformerClassName = "AnalysisAnnotator",
            targetDir = "gen",
            classNameTransformer = { "${it}Analysis" },
            interfaceMap = mapOf("SemanticExpression" to "ExpressionAnalysis<T>", "SemanticStatement" to "StatementAnalysis<T>"),
            interfaceInheritance = mapOf("SemanticStatement" to listOf("SemanticExpression")),
            rootInterfaces = listOf("SemanticStatement"),
            rootTypeName = "SemanticElement",
            rootResultType = "StatementAnalysis",
            newParamName = "lastAnalysis",
            newParamType = "MonotonicAnalysis<T>",
            headerLines = listOf(
                    "import moklev.compiler.compilation.analysis.*",
                    "import moklev.compiler.compilation.MonotonicAnalysis",
                    "import moklev.compiler.semantic.impl.*"
            ),
            transformerHeaderLines = listOf(
                    "import moklev.compiler.compilation.*",
                    "import moklev.compiler.compilation.analysis.impl.*",
                    "import moklev.compiler.compilation.analysis.*",
                    "import moklev.compiler.semantic.*",
                    "import moklev.compiler.semantic.impl.*"
            )
    ).run()

    println("Generating visitor for non-basic annotated semantic elements...")
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/compilation/analysis",
            pathToSourcesImpl = "gen/moklev/compiler/compilation/analysis/impl",
            trimInterface = { it!!.removeSuffix("<T>").removeSuffix("Analysis") },
            trimImpl = { it!!.removeSuffix("Analysis") },
            withTypeParameter = true,
            typeParameterBound = "MonotonicAnalysis<T>",
            methodPrefix = "analyse",
            methodParamName = "element",
            additionalParamsMap = { listOf("input: T") },
            exceptionName = "RuntimeException",
            passElementToException = false,
            interfaceMap = mapOf(
                    "StatementAnalysis<T>" to "Pair<T, Boolean>"
            ),
            ignoreInterfaces = setOf("ExpressionAnalysis<T>"),
            isAvailableAnnotation = { "BasicStatement" !in it },
            onlyImplInterfaces = setOf("StatementAnalysis<T>"),
            targetPackage = "moklev.compiler.compilation.analysis",
            targetDir = "gen",
            headerLines = listOf(
                    "import moklev.compiler.compilation.MonotonicAnalysis",
                    "import moklev.compiler.compilation.analysis.impl.*"
            ),
            generatedClassName = "SomeAnalyzer"
    ).run()

    println("Generating visitor for basic annotated semantic elements...")
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/compilation/analysis",
            pathToSourcesImpl = "gen/moklev/compiler/compilation/analysis/impl",
            trimInterface = { it!!.removeSuffix("<T>").removeSuffix("Analysis") },
            trimImpl = { it!!.removeSuffix("Analysis") },
            withTypeParameter = true,
            typeParameterBound = "MonotonicAnalysis<T>",
            methodPrefix = "analyse",
            methodParamName = "element",
            additionalParamsMap = { listOf() },
            exceptionName = "RuntimeException",
            passElementToException = false,
            interfaceMap = mapOf(
                    "StatementAnalysis<T>" to "T"
            ),
            ignoreInterfaces = setOf(),
            isAvailableAnnotation = { "BasicStatement" in it },
            onlyImplInterfaces = setOf("StatementAnalysis<T>"),
            targetPackage = "moklev.compiler.compilation.analysis",
            targetDir = "gen",
            headerLines = listOf(
                    "import moklev.compiler.compilation.MonotonicAnalysis",
                    "import moklev.compiler.compilation.analysis.impl.*"
            ),
            generatedClassName = "SomeBasicAnalyzer"
    ).run()
}