package moklev.compiler.semantic

/**
 * @author Moklev Vyacheslav
 */
interface SemanticStatement : SemanticElement {
    companion object {
        val Stub = object : SemanticStatement {}
    }
}