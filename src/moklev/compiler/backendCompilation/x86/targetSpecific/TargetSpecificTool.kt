package moklev.compiler.backendCompilation.x86.targetSpecific

import moklev.compiler.backendCompilation.x86.X86Register
import moklev.compiler.backendCompilation.x86.X86StaticValue
import moklev.compiler.semantic.impl.ClassDeclaration

interface TargetSpecificTool {
    fun makeLabel(classDeclaration: ClassDeclaration?, functionName: String): String
    fun parameterLocation(index: Int): X86StaticValue
    val volatileRegisters: Set<X86Register>
}