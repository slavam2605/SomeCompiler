package moklev.integrational.evaluation

import org.junit.Test
import java.util.*

/**
 * @author Moklev Vyacheslav
 */
class ComplexTest(testMode: EvaluationTestMode) : EvaluationTestBase(testMode) {
    companion object {
        const val seed = 7484987838L
    }
    
    @Test
    fun testMergeSort() {
        val program = """
            fun get(a: int64[], index: int64): int64 {
                return *(a + index);
            }
            
            fun set(a: int64[], index: int64, value: int64): int64 {
                *(a + index) = value;
                return 0;
            }
            
            fun merge(a: int64[], b: int64[], l: int64, m: int64, r: int64): int64[] {
                var pl: int64;
                var pr: int64;
                var p: int64;
                pl = l;
                pr = m;
                p = l;
                while (pl < m && pr < r) {
                    if (get(a, pl) < get(a, pr)) {
                        set(b, p, get(a, pl));
                        pl = pl + 1;
                        p = p + 1;
                    } else {
                        set(b, p, get(a, pr));
                        pr = pr + 1;
                        p = p + 1;
                    }
                }
                while (pl < m) {
                    set(b, p, get(a, pl));
                    pl = pl + 1;
                    p = p + 1;
                }
                while (pr < r) {
                    set(b, p, get(a, pr));
                    pr = pr + 1;
                    p = p + 1;
                }
                p = l;
                while (p < r) {
                    set(a, p, get(b, p));
                    p = p + 1;
                }
                return a;
            }
            
            fun _sort(a: int64[], b: int64[], l: int64, r: int64): int64[] {
                if (r - l == 1) {
                    return a;
                }
                if (r - l == 2) {
                    if (get(a, r - 1) < get(a, l)) {
                        var t: int64;
                        t = get(a, l);
                        set(a, l, get(a, r - 1));
                        set(a, r - 1, t);
                        return a;
                    }
                }
                var m: int64;
                m = (l + r) / 2;
                _sort(a, b, l, m);
                _sort(a, b, m, r);
                merge(a, b, l, m, r);
                return a;
            }
            
            fun sort(a: int64[], size: int64): int64[] {
                var temp: int64[];
                temp = createInt64Array(size);
                return _sort(a, temp, 0, size);
            }
        """
        val random = Random(seed)
        for (iter in 1 until 100) {
            val array = IntArray(iter) { random.nextInt(1000000) }
            val sortedArray = array.sortedArray()
            runTest(
                    program + """
                        fun test(): int64[] {
                            var a: int64[];
                            a = createInt64Array($iter);
                            ${array.mapIndexed { index, value -> "*(a + $index) = $value;" }.joinToString(separator = "\n")}
                            sort(a, $iter);
                            return a;
                        }
                    """, 
                    "test()",
                    "ArrayPointer[0, ${sortedArray.contentToString()}]"
            )
        }
    }
}