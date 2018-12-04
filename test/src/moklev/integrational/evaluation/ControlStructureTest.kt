package moklev.integrational.evaluation

import org.junit.Test
import kotlin.math.roundToInt

/**
 * @author Moklev Vyacheslav
 */
class ControlStructureTest(testMode: EvaluationTestMode) : EvaluationTestBase(testMode) {
    @Test
    fun testSimpleControlStructures() {
        runTest("fun foo(x: int64): int64 { if (x < 0) { return 1; } else { return 2; } }", "foo(0-10)", "Int64[1]")
        runTest("fun foo(x: int64): int64 { if (x < 0) { return 1; } else { return 2; } }", "foo(10)", "Int64[2]")
        runTest("fun foo(x: int64): int64 { var a: int64; a = 1; while (0 < x) { a = a * x; x = x - 1; } return a; }", 
                "foo(5)", "Int64[120]")
    }
    
    @Test
    fun testBinarySearch() {
        val sqrtProgram = """
            fun sqrt(n: int64): int64 {
                var l: int64;
                var r: int64;
                var m: int64;
                l = 0;
                r = n;
                while (1 < r - l) {
                    m = (l + r) / 2;
                    if (m * m < n) {
                        l = m;
                    } else {
                        r = m;
                    }
                }
                var errL: int64;
                var errR: int64;
                errL = n - l * l;
                errR = n - r * r;
                if (errL < 0) { errL = 0 - errL; }
                if (errR < 0) { errR = 0 - errR; }
                if (errL < errR) {
                    return l;
                } else {
                    return r;
                }
            }
        """
        for (i in 1 until 10000) {
            runTest(sqrtProgram, "sqrt($i)", "Int64[${Math.sqrt(i.toDouble()).roundToInt()}]")
        }
    }
}