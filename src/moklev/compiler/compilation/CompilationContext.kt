package moklev.compiler.compilation

import moklev.compiler.semantic.impl.ClassDeclaration

sealed class CompilationContext

object TopLevelContext : CompilationContext()

class ClassDeclarationContext(val outerClass: ClassDeclaration) : CompilationContext()