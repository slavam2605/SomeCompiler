package moklev.compiler.types

/**
 * @author Moklev Vyacheslav
 */
class ArrayPointerType(sourceType: Type) : PointerType(sourceType) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) 
            return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}