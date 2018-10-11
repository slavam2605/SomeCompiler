package moklev.intergational.compilation

import moklev.compiler.compilation.DiagnosticCompilationErrors.AssignTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationArgumentTypeMismatchError
import moklev.compiler.compilation.DiagnosticCompilationErrors.InvocationWrongNumberOfArgumentsError
import moklev.compiler.compilation.DiagnosticCompilationErrors.ReturnTypeMismatchError
import moklev.compiler.types.ArrayPointerType
import moklev.compiler.types.ScalarType
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
        """, AssignTypeMismatchError(ArrayPointerType(ScalarType.INT64), ScalarType.INT64))
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
        """, AssignTypeMismatchError(ScalarType.INT64, ArrayPointerType(ScalarType.INT64)))
    }
    
    @Test
    fun testReturnValueType() {
        runTestError("""
            fun foo(): int64 {
                return 1 < 2;
            }
        """, ReturnTypeMismatchError(ScalarType.INT64, ScalarType.BOOLEAN))
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
        """, InvocationArgumentTypeMismatchError(0, ScalarType.INT64, ScalarType.BOOLEAN))
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
        """, InvocationWrongNumberOfArgumentsError(2, 1))
    }
}