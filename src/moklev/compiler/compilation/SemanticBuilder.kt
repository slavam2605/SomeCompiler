package moklev.compiler.compilation

import moklev.compiler.ast.ASTNode
import moklev.compiler.ast.impl.BinaryOperation
import moklev.compiler.ast.impl.Constant
import moklev.compiler.exceptions.CompilationException
import moklev.compiler.semantic.SemanticElement
import moklev.compiler.semantic.impl.DoubleConstant
import moklev.compiler.semantic.impl.Int64BinaryOperation
import moklev.compiler.semantic.impl.Int64Constant
import moklev.compiler.types.ScalarType

/**
 * @author Moklev Vyacheslav
 */
class SemanticBuilder {
    fun build(root: ASTNode): SemanticElement {
        if (root is Constant)
            return buildConstant(root)
        if (root is BinaryOperation)
            return buildBinaryOperation(root)
        throw CompilationException(root, "`root` is not a SemanticASTNode")
    }
    
    fun buildConstant(node: Constant): SemanticElement {
        node.value.toLongOrNull()?.let { 
            return Int64Constant(it)
        }
        node.value.toDoubleOrNull()?.let { 
            return DoubleConstant(it)
        }
        throw CompilationException(node, "Unknown constant: \"${node.value}\"")
    }
    
    fun buildBinaryOperation(node: BinaryOperation): SemanticElement {
        val left = build(node.left)
        val right = build(node.right)
        if (left.type != right.type)
            throw CompilationException(node, "Types of left and right operands are different: ${left.type} and ${right.type}")
        when (left.type) {
            ScalarType.INT64 -> return Int64BinaryOperation(node.op, left, right) 
            else -> throw CompilationException(node, "Unknown type of operands: ${left.type}")
        }
    }
}