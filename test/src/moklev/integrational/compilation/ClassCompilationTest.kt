package moklev.integrational.compilation

import org.junit.Test

class ClassCompilationTest : CompilationTestBase() {
    @Test
    fun testRecursiveClass() {
        runTestSucceeded("""
            class TreeNode {
                var data: int64;
                var left: TreeNode*;
                var right: TreeNode*;
            }
        """)
    }
}