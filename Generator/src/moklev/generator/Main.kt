package moklev.generator

/**
 * @author Moklev Vyacheslav
 */
fun main(args: Array<String>) {
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/ast",
            pathToSourcesImpl = "src/moklev/compiler/ast/impl",
            trimInterface = { it!!.removeSuffix("ASTNode") },
            trimImpl = { it!!.removeSuffix("Node") },
            withTypeParameter = false,
            typeParameterBound = "",
            methodPrefix = "build",
            methodParamName = "node",
            exceptionName = "CompilationException",
            passElementToException = true,
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
            pathToSourcesImpl = "src/moklev/compiler/semantic/impl",
            trimInterface = { it!!.removePrefix("Semantic") },
            trimImpl = { it!! },
            withTypeParameter = false,
            typeParameterBound = "",
            methodPrefix = "evaluate",
            methodParamName = "element",
            exceptionName = "EvaluationException",
            passElementToException = true,
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
    
    ClassTransformerWorker(
            pathToSources = "src/moklev/compiler/semantic/impl",
            targetPackage = "moklev.compiler.compilation.analysis.impl",
            transformerPackage = "moklev.compiler.compilation.analysis",
            transformerClassName = "AnalysisAnnotator",
            targetDir = "gen",
            classNameTransformer = { "${it}Analysis" },
            interfaceMap = mapOf("SemanticExpression" to "ExpressionAnalysis", "SemanticStatement" to "StatementAnalysis"),
            interfaceInheritance = mapOf("SemanticStatement" to listOf("SemanticExpression")),
            rootInterfaces = listOf("SemanticStatement"),
            rootTypeName = "SemanticElement",
            rootResultType = "StatementAnalysis",
            newParamName = "lastAnalysis",
            newParamType = "MonotonicAnalysis",
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
    
    GeneratorWorker(
            pathToSources = "src/moklev/compiler/compilation/analysis",
            pathToSourcesImpl = "gen/moklev/compiler/compilation/analysis/impl",
            trimInterface = { it!!.removeSuffix("Analysis") },
            trimImpl = { it!!.removeSuffix("Analysis") },
            withTypeParameter = true,
            typeParameterBound = "MonotonicAnalysis",
            methodPrefix = "analyse",
            methodParamName = "element",
            exceptionName = "RuntimeException",
            passElementToException = false,
            interfaceMap = mapOf(
                    "ExpressionAnalysis" to "ExpressionAnalysis",
                    "StatementAnalysis" to "StatementAnalysis"
            ),
            onlyImplInterfaces = setOf(),
            targetPackage = "moklev.compiler.compilation.analysis",
            targetDir = "gen",
            headerLines = listOf(
                    "import moklev.compiler.compilation.MonotonicAnalysis",
                    "import moklev.compiler.compilation.analysis.impl.*"
            ),
            generatedClassName = "SomeAnalyzer"
    ).run()
}