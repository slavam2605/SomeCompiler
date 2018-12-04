package moklev.compiler.backendCompilation.x86.targetSpecific

import moklev.compiler.backendCompilation.x86.*
import moklev.compiler.semantic.impl.ClassDeclaration

object WindowsSpecificTool : TargetSpecificTool {
    override fun makeLabel(classDeclaration: ClassDeclaration?, functionName: String): String {
        val classPrefix = classDeclaration?.name?.let { "$it$" } ?: ""
        return "$classPrefix$functionName"
    }

    override fun parameterLocation(index: Int): X86StaticValue {
        return when (index) {
            0 -> RCX
            1 -> RDX
            2 -> R8
            3 -> R9
            else -> TODO("on stack")
        }
    }

    override val volatileRegisters = setOf(RAX, RCX, RDX, R8, R9, R10, R11)
}