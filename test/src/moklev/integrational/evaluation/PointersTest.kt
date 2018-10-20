package moklev.integrational.evaluation

import org.junit.Test

/**
 * @author Moklev Vyacheslav
 */
class PointersTest : EvaluationTestBase() {
    @Test
    fun testLocalVariablePointer() {
        runTest(
                """
                    fun foo(): int64 {
                        var x: int64;
                        var p: int64*;
                        x = 10;
                        p = &x;
                        *p = 7;
                        return x;
                    }
                """,
                "foo()",
                "Int64[7]"
        )
        runTest(
                """
                    fun foo(x: int64): int64 {
                        var p: int64*;
                        p = &x;
                        *p = 9;
                        return x;
                    }
                """,
                "foo(1)",
                "Int64[9]"
        )
        runTest(
                """
                    fun foo(): int64 {
                        var p: int64*;
                        var x: int64;
                        x = 0;
                        if (1 < 2) {
                            var x: int64;
                            x = 0;
                            p = &x;
                            if (1 < 2) {
                                var x: int64;
                                x = 0;
                                *p = 100;
                            }
                            return x;
                        }
                        return 0;
                    }
                """,
                "foo()",
                "Int64[100]"
        )
    }
    
    @Test
    fun testCrossFunctionPointer() {
        runTest(
                """
                    fun foo(): int64 {
                        var x: int64;
                        bar(&x);
                        return x;
                    }
                    
                    fun bar(p: int64*): int64 {
                        *p = 200;
                        return 0;
                    }
                """,
                "foo()",
                "Int64[200]"
        )
        runTest(
                """
                    fun foo(): int64 {
                        var x: int64;
                        return bar(&x, 1);
                    }
                    
                    fun bar(p: int64*, mode: int64): int64 {
                        if (mode == 1) {
                            var a: int64;
                            bar(&a, 2);
                            return a;
                        } else {
                            *p = 123;
                            return 0;
                        }
                    }
                """,
                "foo()",
                "Int64[123]"
        )
    }
}