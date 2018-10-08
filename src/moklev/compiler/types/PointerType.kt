package moklev.compiler.types

/**
 * @author Moklev Vyacheslav
 */
open class PointerType(val sourceType: Type) : Type {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass)
            return false
        return (other as PointerType).sourceType == sourceType
    }

    override fun hashCode(): Int {
        return sourceType.hashCode()
    }
}