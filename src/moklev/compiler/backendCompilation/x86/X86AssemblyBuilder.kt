package moklev.compiler.backendCompilation.x86

class X86AssemblyBuilder {
    val lines = mutableListOf<String>()

    fun label(name: String) {
        lines.add("$name:")
    }

    fun push(value: X86StaticValue) {
        lines.add("pushq ${value.toAssemblyString()}")
    }

    fun pop(value: X86StaticValue) {
        lines.add("popq ${value.toAssemblyString()}")
    }

    fun mov(dest: X86StaticValue, src: X86StaticValue) {
        lines.add("movq ${src.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun add(dest: X86StaticValue, value: X86StaticValue) {
        lines.add("addq ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun sub(dest: X86StaticValue, value: X86StaticValue) {
        lines.add("subq ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun imul(dest: X86StaticValue, value: X86StaticValue) {
        lines.add("imulq ${value.toAssemblyString()}, ${dest.toAssemblyString()}")
    }

    fun idiv(value: X86StaticValue) {
        lines.add("idivq ${value.toAssemblyString()}")
    }

    fun cqo() {
        lines.add("cqo")
    }

    fun ret() {
        lines.add("ret")
    }

    fun directiveGlobal(name: String) {
        lines.add(".globl $name")
    }
}