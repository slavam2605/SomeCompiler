package moklev.compiler.backendCompilation.x86

class X86AssemblyBuilder {
    val lines = mutableListOf<String>()

    fun label(name: String) {
        lines.add("$name:")
    }

    fun push(value: X86StaticValue) {
        val sizeSuffix = sizeLetter(value.size)
        lines.add("push$sizeSuffix ${value.toAssemblyString()}")
    }

    fun pop(value: X86StaticValue) {
        val sizeSuffix = sizeLetter(value.size)
        lines.add("pop$sizeSuffix ${value.toAssemblyString()}")
    }

    fun mov(dest: X86StaticValue, src: X86StaticValue) {
        val sizeSuffix = sizeLetter(commonSize(dest.size, src.size))
        lines.add("mov$sizeSuffix ${src.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun add(dest: X86StaticValue, value: X86StaticValue) {
        val sizeSuffix = sizeLetter(commonSize(dest.size, value.size))
        lines.add("add$sizeSuffix ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun sub(dest: X86StaticValue, value: X86StaticValue) {
        val sizeSuffix = sizeLetter(commonSize(dest.size, value.size))
        lines.add("sub$sizeSuffix ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun imul(dest: X86StaticValue, value: X86StaticValue) {
        val sizeSuffix = sizeLetter(commonSize(dest.size, value.size))
        lines.add("imul$sizeSuffix ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun idiv(value: X86StaticValue) {
        val sizeSuffix = sizeLetter(value.size)
        lines.add("idiv$sizeSuffix ${value.toAssemblyString()}")
    }

    fun cqo() {
        lines.add("cqo")
    }

    fun ret() {
        lines.add("ret")
    }

    fun cmp(left: X86StaticValue, right: X86StaticValue) {
        val sizeSuffix = sizeLetter(commonSize(left.size, right.size))
        lines.add("cmp$sizeSuffix ${right.toAssemblyString()}, ${left.toAssemblyString()}")
    }

    fun cmov(op: String, dest: X86StaticValue, value: X86StaticValue) {
        val sizeSuffix = sizeLetter(commonSize(dest.size, value.size))
        lines.add("cmov$op$sizeSuffix ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun set(flag: String, dest: X86StaticValue) {
        require(dest.size == 1)
        lines.add("set$flag ${dest.toAssemblyString()}")
    }

    fun movzx(dest: X86StaticValue, value: X86StaticValue) {
        val valueSize = sizeLetter(value.size)
        val destSize = sizeLetter(dest.size)
        lines.add("movz$valueSize$destSize ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun directiveGlobal(name: String) {
        lines.add(".globl $name")
    }
}