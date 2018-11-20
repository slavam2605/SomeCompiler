package moklev.compiler.types

/**
 * @author Moklev Vyacheslav
 */
class ArrayPointerType(sourceType: Type) : PointerType(sourceType) {
    override fun equals(other: Any?): Boolean {
        if (other?.javaClass != ArrayPointerType::class.java)
            return false
        return (other as PointerType).sourceType == sourceType
    }

    override fun hashCode(): Int {
        return sourceType.hashCode()
    }
}