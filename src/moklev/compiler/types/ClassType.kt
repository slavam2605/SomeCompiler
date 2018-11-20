package moklev.compiler.types

import moklev.compiler.semantic.impl.ClassDeclaration

/**
 * @author Moklev Vyacheslav
 */
class ClassType(val declaration: ClassDeclaration) : Type {
    override fun equals(other: Any?): Boolean {
        if (other?.javaClass != ClassType::class.java)
            return false
        return declaration.name == (other as ClassType).declaration.name
    }

    override fun hashCode(): Int {
        return declaration.name.hashCode()
    }
}