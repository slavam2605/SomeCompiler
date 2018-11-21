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
import moklev.compiler.types.*

/**
 * @author Moklev Vyacheslav
 */
class SemanticBuilder : SomeBuilder {
    val symbolResolver = SymbolResolver()
    val typeResolver = TypeResolver(symbolResolver)
    val semanticAnalyzer = SemanticAnalyzer()

    override fun buildQualifiedSymbol(node: QualifiedSymbolNode, context: CompilationContext): SemanticExpression {
        val target = node.target?.let { buildExpression(it, context) }
        return symbolResolver.resolveSymbol(target, node.name, context)
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

    override fun buildAddressOf(node: AddressOfNode, context: CompilationContext): SemanticExpression {
        val target = buildExpression(node.target, context)
        if (target is LocalVariableReference)
            return AddressOf(target)
        throw CompilationException(node, AddressOfError(target))
    }

    override fun buildDereference(node: DereferenceNode, context: CompilationContext): SemanticExpression {
        val target = buildExpression(node.target, context)
        if (target.type is PointerType)
            return Dereference(target)
        throw CompilationException(node, DereferenceTypeError(target.type))
    }

    override fun buildArrayPointerType(node: ArrayPointerTypeNode, context: CompilationContext): Type {
        val sourceType = buildType(node.sourceType, context)
        return ArrayPointerType(sourceType)
    }

    override fun buildPointerType(node: PointerTypeNode, context: CompilationContext): Type {
        val sourceType = buildType(node.sourceType, context)
        return PointerType(sourceType)
    }

    override fun buildScalarType(node: ScalarTypeNode, context: CompilationContext): Type {
        if (context is ClassDeclarationContext && context.outerClass.name == node.name)
            return ClassType(context.outerClass)
        return typeResolver.resolveType(node.name)
    }

    fun buildDeclarationStub(root: DeclarationASTNode, context: CompilationContext): SemanticDeclaration {
        if (root is FunctionDeclarationNode)
            return buildFunctionDeclarationStub(root, context)
        if (root is ClassDeclarationNode)
            return buildClassDeclarationStub(root, context)
        if (root is FieldDeclarationNode)
            return buildFieldDeclarationStub(root, context)
        TODO("Generate additional methods for creating stubs")
    }

    override fun buildInvocation(node: InvocationNode, context: CompilationContext): SemanticExpression {
        val target = buildExpression(node.target, context)
        val targetParameters = when (target) {
            is MethodReference -> listOf("this" to PointerType(target.targetType)) + target.declaration.parameters
            is FunctionReference -> target.declaration.parameters
            is ConstructorReference -> target.declaration.fields.map { it.name to it.type }
            else -> throw CompilationException(node, InvocationTargetError(target))
        }
        var parameters = node.parameters.map { buildExpression(it, context) }
        if (target is MethodReference) {
            parameters = listOf(AddressOf(target.target)) + parameters
        }
        if (parameters.size != targetParameters.size)
            throw CompilationException(node, InvocationWrongNumberOfArgumentsError(targetParameters.size, parameters.size))
        for (index in 0 until parameters.size) {
            if (parameters[index].type != targetParameters[index].second)
                throw CompilationException(node, InvocationArgumentTypeMismatchError(index, targetParameters[index].second, parameters[index].type))
        }
        return Invocation(target, parameters)
    }

    override fun buildReturn(node: ReturnNode, context: CompilationContext): SemanticStatement {
        val value = buildExpression(node.value, context)
        val function = (context as? FunctionBodyContext)?.declaration
            ?: throw CompilationException(node, "No current function")
        if (value.type != function.returnType)
            throw CompilationException(node, ReturnTypeMismatchError(function.returnType, value.type))
        return Return(value)
    }

    fun buildFieldDeclarationStub(node: FieldDeclarationNode, context: CompilationContext): FieldDeclaration {
        val type = buildType(node.type, context)
        return FieldDeclaration(node.name, type)
    }
    
    fun buildClassDeclarationStub(node: ClassDeclarationNode, context: CompilationContext): ClassDeclaration {
        val fields = mutableListOf<FieldDeclaration>()
        val methods = mutableListOf<FunctionDeclaration>()
        val declaration = ClassDeclaration(node.name, fields, methods)
        val newContext = ClassDeclarationContext(declaration)

        val innerStubs = node.innerDeclarations.map { buildDeclarationStub(it, newContext) }
        fields.addAll(innerStubs.filterIsInstance<FieldDeclaration>())
        methods.addAll(innerStubs.filterIsInstance<FunctionDeclaration>())
        if (fields.size + methods.size != innerStubs.size)
            throw CompilationException(node, "Unknown declarations in class ${node.name}")

        return declaration
    }
    
    fun buildFunctionDeclarationStub(node: FunctionDeclarationNode, context: CompilationContext): FunctionDeclaration {
        val parameters = node.parameters.map { (name, type) -> name to buildType(type, context) }
        val returnType = buildType(node.returnType, context)
        return FunctionDeclaration(node.name, parameters, returnType)
    }

    override fun buildFunctionDeclaration(node: FunctionDeclarationNode, context: CompilationContext) {
        val stub = when (context) {
            is TopLevelContext -> symbolResolver.declaredFunctions[node.name]
            is ClassDeclarationContext -> context.outerClass.methods.find { it.name == node.name }
            else -> throw CompilationException(node, "Unexpected context for function declaration: `${context.javaClass}`")
        } ?: throw CompilationException(node, "No predeclared stub found")
        val newContext = when (context) {
            is ClassDeclarationContext -> MethodBodyContext(ClassType(context.outerClass), stub)
            else -> FunctionBodyContext(stub)
        }
        val body = symbolResolver.withFunctionScope {
            stub.parameters.forEach { (name, type) ->
                symbolResolver.declareVariable(name, type)
            }
            buildStatement(node.body, newContext)
        }
        stub.complete(body)
        semanticAnalyzer.analyzeFunctionDeclaration(stub)
    }

    override fun buildDeclarationList(node: DeclarationListNode, context: CompilationContext) {
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
            val stub = buildDeclarationStub(declaration, context)
            symbolResolver.declare(stub)
        }
        for (declaration in node.declarations) {
            buildDeclaration(declaration, context)
        }
    }

    override fun buildIf(node: IfNode, context: CompilationContext): SemanticStatement {
        val condition = buildExpression(node.condition, context)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, IfConditionBooleanTypeError(condition.type))
        val bodyTrue = symbolResolver.withScope {
            buildStatement(node.bodyTrue, context)
        }
        val bodyFalse = symbolResolver.withScope {
            buildStatement(node.bodyFalse, context)
        }
        return If(condition, bodyTrue, bodyFalse)
    }

    override fun buildWhile(node: WhileNode, context: CompilationContext): SemanticStatement {
        val condition = buildExpression(node.condition, context)
        if (condition.type != ScalarType.BOOLEAN)
            throw CompilationException(node, WhileConditionBooleanTypeError(condition.type))
        val body = symbolResolver.withScope {
            buildStatement(node.body, context)
        }
        return While(condition, body)
    }

    override fun buildAssignment(node: AssignmentNode, context: CompilationContext): Assignment {
        val value = buildExpression(node.value, context)
        val target = buildExpression(node.target, context)
        if (value.type != target.type)
            throw CompilationException(node, AssignTypeMismatchError(target.type, value.type))
        return Assignment(target, value)
    }

    override fun buildStatementList(node: StatementListNode, context: CompilationContext): StatementList {
        val statements = node.statements.map { buildStatement(it, context) }
        return StatementList(statements)
    }

    override fun buildVariableDeclaration(node: VariableDeclarationNode, context: CompilationContext): VariableDeclaration {
        val type = buildType(node.type, context)
        symbolResolver.declareVariable(node.name, type)
        return VariableDeclaration(node.name, type)
    }

    override fun buildConstant(node: ConstantNode, context: CompilationContext): SemanticExpression {
        node.value.toLongOrNull()?.let { 
            return Int64Constant(it)
        }
        node.value.toDoubleOrNull()?.let { 
            return DoubleConstant(it)
        }
        throw CompilationException(node, "Unknown constant: \"${node.value}\"")
    }

    override fun buildBinaryOperation(node: BinaryOperationNode, context: CompilationContext): SemanticExpression {
        val left = buildExpression(node.left, context)
        val right = buildExpression(node.right, context)
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