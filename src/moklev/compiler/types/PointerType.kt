package moklev.compiler.types

/**
 * @author Moklev Vyacheslav
 */
class PointerType(val sourceType: Type) : Type {
    override fun equals(other: Any?): Boolean {
        if (other !is PointerType)
            return false
        return other.sourceType == sourceType
    }

    override fun hashCode(): Int {
        return sourceType.hashCode()
    }
}