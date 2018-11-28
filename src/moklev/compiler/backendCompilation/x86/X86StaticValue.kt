package moklev.compiler.backendCompilation.x86

sealed class X86StaticValue {
    abstract fun toAssemblyString(): String
}

class X86Register(val name: String) : X86StaticValue() {
    override fun toAssemblyString(): String = name
}

class IntegerLiteral(val value: String) : X86StaticValue() {
    override fun toAssemblyString(): String = "$$value"
}

val RAX = X86Register("%rax")
val RBX = X86Register("%rbx")
val RCX = X86Register("%rcx")
val RDX = X86Register("%rdx")
val RSI = X86Register("%rsi")
val RDI = X86Register("%rdi")
val RSP = X86Register("%rsp")
val RBP = X86Register("%rbp")
val R8 = X86Register("%r8")
val R9 = X86Register("%r9")
val R10 = X86Register("%r10")
val R11 = X86Register("%r11")
val R12 = X86Register("%r12")
val R13 = X86Register("%r13")
val R14 = X86Register("%r14")
val R15 = X86Register("%r15")