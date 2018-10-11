package moklev.intergational.compilation

import org.junit.Test

/**
 * @author Moklev Vyacheslav
 */
class TypeCheckTest : CompilationTestBase() {
    @Test
    fun testAssign() {
        runTestError("""
            fun foo(): int64 {
                var x: int64[];
                x = 2;
                return 1;
            }
        """)
    }
    
    @Test
    fun testFunctionReturnType() {
        runTestError("""
            fun foo(): int64[] {
                return createInt64Array(0);
            }
            
            fun bar(): int64 {
                var x: int64;
                x = foo();
                return 0;
            }
        """)
    }
    
    @Test
    fun testReturnValueType() {
        runTestError("""
            fun foo(): int64 {
                return 1 < 2;
            }
        """)
    }
    
    @Test
    fun testArgumentsType() {
        runTestError("""
            fun foo(x: int64): int64 {
                return x;
            }
            
            fun bar(): int64 {
                return foo(1 < 2);
            }
        """)
    }
    
    @Test
    fun testArgumentsCount() {
        runTestError("""
            fun foo(x: int64, y: int64): int64 {
                return 0;
            }
            
            fun bar(): int64 {
                return foo(1);
            }
        """)
    }
}