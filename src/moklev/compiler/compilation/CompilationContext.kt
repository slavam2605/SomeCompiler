package moklev.compiler.compilation

import moklev.compiler.semantic.impl.ClassDeclaration
import moklev.compiler.semantic.impl.FunctionDeclaration
import moklev.compiler.types.ClassType

sealed class CompilationContext

object TopLevelContext : CompilationContext()

class ClassDeclarationContext(val outerClass: ClassDeclaration) : CompilationContext()

open class FunctionBodyContext(val declaration: FunctionDeclaration) : CompilationContext()

class MethodBodyContext(val targetType: ClassType, declaration: FunctionDeclaration) : FunctionBodyContext(declaration)