package moklev.compiler.backendCompilation.x86

sealed class X86StaticValue(val size: Int?) {
    abstract fun toAssemblyString(): String
}

class X86Register(val name: String, size: Int) : X86StaticValue(size) {
    override fun toAssemblyString(): String = name
}

class X86LocalStackElement(val offset: Int, size: Int) : X86StaticValue(size) {
    override fun toAssemblyString(): String = "${-offset}(${RBP.toAssemblyString()})"
}

class X86IntegerLiteral(val value: String, size: Int? = null) : X86StaticValue(size) {
    override fun toAssemblyString(): String = "$$value"

    override fun equals(other: Any?) = (other as? X86IntegerLiteral)?.value == value
    override fun hashCode() = value.hashCode()
}

val RAX = X86Register("%rax", 8)
val RBX = X86Register("%rbx", 8)
val RCX = X86Register("%rcx", 8)
val RDX = X86Register("%rdx", 8)
val RSI = X86Register("%rsi", 8)
val RDI = X86Register("%rdi", 8)
val RSP = X86Register("%rsp", 8)
val RBP = X86Register("%rbp", 8)
val R8 = X86Register("%r8", 8)
val R9 = X86Register("%r9", 8)
val R10 = X86Register("%r10", 8)
val R11 = X86Register("%r11", 8)
val R12 = X86Register("%r12", 8)
val R13 = X86Register("%r13", 8)
val R14 = X86Register("%r14", 8)
val R15 = X86Register("%r15", 8)

val AL = X86Register("%al", 1)