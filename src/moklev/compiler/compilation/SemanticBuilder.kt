package moklev.compiler.compilation

import moklev.compiler.ast.DeclarationASTNode
import moklev.compiler.ast.impl.*
import moklev.compiler.compilation.DiagnosticCompilationErrors.AddressOfError
import moklev.compiler.compilation.DiagnosticCompilationErrors.AssignTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.DereferenceTypeError
import moklev.compiler.compilation.DiagnosticCompilationErrors.IfConditionBooleanTypeError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationArgumentTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationTargetError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationWrongNumberOfArgumentsError
import moklev.compiler.compilation.DiagnosticCompilationErrors.NoSuchBinaryOperationError
import moklev.compiler.compilation.DiagnosticCompilationErrors.ReturnTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.WhileConditionBooleanTypeError
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticDeclaration
import moklev.compiler.semantic.SemanticExpression
import moklev.compiler.semantic.SemanticStatement
import moklev.compiler.semantic.impl.*
import moklev.compiler.types.ArrayPointerType
import moklev.compiler.types.PointerType
import moklev.compiler.types.ScalarType
import moklev.compiler.types.Type

/**
 * @author Moklev Vyacheslav
 */
class SemanticBuilder : SomeBuilder {
    val symbolResolver = SymbolResolver()
    val typeResolver = TypeResolver(symbolResolver)
    val semanticAnalyzer = SemanticAnalyzer()
    var currentFunction: FunctionDeclaration? = null

    private inline fun <T> withFunction(function: FunctionDeclaration, body: () -> T): T {
        currentFunction = function
        try {
            return body()
        } finally {
            currentFunction = null
        }
    }

    override fun buildQualifiedSymbol(node: QualifiedSymbolNode): SemanticExpression {
        val target = node.target?.let { buildExpression(it) }
        return symbolResolver.resolveSymbol(target, node.name)
    }
    
    override fun buildFieldDeclaration(node: FieldDeclarationNode, context: CompilationContext) = Unit

    override fun buildClassDeclaration(node: ClassDeclarationNode, context: CompilationContext) {
        val declarations = node.innerDeclarations
        val classStub = symbolResolver.declaredClasses[node.name]
                ?: throw CompilationException(node, "No precompiled stub found")
        val newContext = ClassDeclarationContext(classStub)
        for (declaration in declarations) {
            buildDeclaration(declaration, newContext)
        }
    }

    override fun buildAddressOf(node: AddressOfNode): SemanticExpression {
        val target = buildExpression(node.target)
        if (target is LocalVariableReference)
            return AddressOf(target)
        throw CompilationException(node, AddressOfError(target))
    }

    override fun buildDereference(node: DereferenceNode): SemanticExpression {
        val target = buildExpression(node.target)
        if (target.type is PointerType)
            return Dereference(target)
        throw CompilationException(node, DereferenceTypeError(target.type))
    }

    override fun buildArrayPointerType(node: ArrayPointerTypeNode): Type {
        val sourceType = buildType(node.sourceType)
        return ArrayPointerType(sourceType)
    }

    override fun buildPointerType(node: PointerTypeNode): Type {
        val sourceType = buildType(node.sourceType)
        return PointerType(sourceType)
    }

    override fun buildScalarType(node: ScalarTypeNode): Type {
        return typeResolver.resolveType(node.name)
    }

    fun buildDeclarationStub(root: DeclarationASTNode): SemanticDeclaration {
        if (root is FunctionDeclarationNode)
            return buildFunctionDeclarationStub(root)
        if (root is ClassDeclarationNode)
            return buildClassDeclarationStub(root)
        if (root is FieldDeclarationNode)
            return buildFieldDeclarationStub(root)
        TODO("Generate additional methods for creating stubs")
    }

    override fun buildInvocation(node: InvocationNode): SemanticExpression {
        val target = buildExpression(node.target)
        val targetParameters = when (target) {
            is FunctionReference -> target.declaration.parameters
            is ConstructorReference -> target.declaration.fields.map { it.name to it.type }
            else -> throw CompilationException(node, InvocationTargetError(target))
        }
        val parameters = node.parameters.map { buildExpression(it) }
        if (parameters.size != targetParameters.size)
            throw CompilationException(node, InvocationWrongNumberOfArgumentsError(targetParameters.size, parameters.size))
        for (index in 0 until parameters.size) {
            if (parameters[index].type != targetParameters[index].second)
                throw CompilationException(node, InvocationArgumentTypeMismatchError(index, targetParameters[index].second, parameters[index].type))
        }
        return Invocation(target, parameters)
    }

    override fun buildReturn(node: ReturnNode): SemanticStatement {
        val value = buildExpression(node.value)
        val function = currentFunction
            ?: throw CompilationException(node, "No current function")
        if (value.type != function.returnType)
            throw CompilationException(node, ReturnTypeMismatchError(function.returnType, value.type))
        return Return(value)
    }

    fun buildFieldDeclarationStub(node: FieldDeclarationNode): FieldDeclaration {
        val type = buildType(node.type)
        return FieldDeclaration(node.name, type)
    }
    
    fun buildClassDeclarationStub(node: ClassDeclarationNode): ClassDeclaration {
        val innerStubs = node.innerDeclarations.map { buildDeclarationStub(it) }
        val fields = innerStubs.filterIsInstance<FieldDeclaration>()
        val methods = innerStubs.filterIsInstance<FunctionDeclaration>()
        if (fields.size + methods.size != innerStubs.size)
            throw CompilationException(node, "Unknown declarations in class ${node.name}")
        return ClassDeclaration(node.name, fields, methods)
    }
    
    fun buildFunctionDeclarationStub(node: FunctionDeclarationNode): FunctionDeclaration {
        val parameters = node.parameters.map { (name, type) -> name to buildType(type) }
        val returnType = buildType(node.returnType)
        return FunctionDeclaration(node.name, parameters, returnType)
    }

    override fun buildFunctionDeclaration(node: FunctionDeclarationNode, context: CompilationContext) {
        val stub = when (context) {
            is TopLevelContext -> symbolResolver.declaredFunctions[node.name]
            is ClassDeclarationContext -> context.outerClass.methods.find { it.name == node.name }
        } ?: throw CompilationException(node, "No predeclared stub found")
        val body = withFunction(stub) {
            symbolResolver.withScope {
                stub.parameters.forEach { (name, type) ->
                    symbolResolver.declareVariable(name, type)
                }
                buildStatement(node.body)
            }
        }
        stub.complete(body)
        semanticAnalyzer.analyzeFunctionDeclaration(stub)
    }

    override fun buildDeclarationList(node: DeclarationListNode) {
        val typeDeclarations = mutableListOf<DeclarationASTNode>()
        val otherDeclarations = mutableListOf<DeclarationASTNode>()
        for (declaration in node.declarations) {
            if (declaration is ClassDeclarationNode) {
                typeDeclarations.add(declaration)
            } else {
                otherDeclarations.add(declaration)
            }
        }
        // build type stubs first to properly resolve types in function declarations
        for (declaration in typeDeclarations + otherDeclarations) {
            val stub = buildDeclarationStub(declaration)
            symbolResolver.declare(stub)
        }
        for (declaration in node.declarations) {
            buildDeclaration(declaration, TopLevelContext)
        }
    }

    override fun buildIf(node: IfNode): SemanticStatement {
        val condition = buildExpression(node.condition)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, IfConditionBooleanTypeError(condition.type))
        val bodyTrue = symbolResolver.withScope {
            buildStatement(node.bodyTrue)
        }
        val bodyFalse = symbolResolver.withScope {
            buildStatement(node.bodyFalse)
        }
        return If(condition, bodyTrue, bodyFalse)
    }

    override fun buildWhile(node: WhileNode): SemanticStatement {
        val condition = buildExpression(node.condition)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, WhileConditionBooleanTypeError(condition.type))
        val body = symbolResolver.withScope {
            buildStatement(node.body)
        }
        return While(condition, body)
    }

    override fun buildAssignment(node: AssignmentNode): Assignment {
        val value = buildExpression(node.value)
        val target = buildExpression(node.target)
        if (value.type != target.type)
            throw CompilationException(node, AssignTypeMismatchError(target.type, value.type))
        return Assignment(target, value)
    }

    override fun buildStatementList(node: StatementListNode): StatementList {
        val statements = node.statements.map { buildStatement(it) }
        return StatementList(statements)
    }

    override fun buildVariableDeclaration(node: VariableDeclarationNode): VariableDeclaration {
        val type = buildType(node.type)
        symbolResolver.declareVariable(node.name, type)
        return VariableDeclaration(node.name, type)
    }

    override fun buildConstant(node: ConstantNode): SemanticExpression {
        node.value.toLongOrNull()?.let { 
            return Int64Constant(it)
        }
        node.value.toDoubleOrNull()?.let { 
            return DoubleConstant(it)
        }
        throw CompilationException(node, "Unknown constant: \"${node.value}\"")
    }

    override fun buildBinaryOperation(node: BinaryOperationNode): SemanticExpression {
        val left = buildExpression(node.left)
        val right = buildExpression(node.right)
        if (left.type is ArrayPointerType && right.type == ScalarType.INT64) {
            return ArrayPointerShiftOperation(node.op, left, right)
        }
        if (left.type != right.type)
            throw CompilationException(node, NoSuchBinaryOperationError(node.op, left.type, right.type))
        when (left.type) {
            ScalarType.INT64 -> return Int64BinaryOperation(node.op, left, right)
            ScalarType.BOOLEAN -> return BooleanBinaryOperation(node.op, left, right)
            else -> throw CompilationException(node, NoSuchBinaryOperationError(node.op, left.type, right.type))
        }
    }
}