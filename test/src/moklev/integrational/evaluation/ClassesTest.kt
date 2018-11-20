package moklev.integrational.evaluation

import org.junit.Test

class ClassesTest : EvaluationTestBase() {
    @Test
    fun testSimpleClass() {
        runTest(
                """
                    class Pair {
                        var x: int64;
                        var y: int64;
                    }
                """,
                "Pair(1, 2)",
                "Class<Pair>{Int64[1], Int64[2]}"
        )
        runTest(
                """
                    class Pair {
                        var x: int64;
                        var y: int64;
                    }
                """,
                "Pair(1, 2).y",
                "Int64[2]"
        )
    }

    @Test
    fun testClassModification() {
        runTest(
                """
                    class Pair {
                        var x: int64;
                        var y: int64;
                    }

                    class NestedPair {
                        var pair: Pair;
                    }

                    class TestResult {
                        var a: Pair;
                        var b: Pair;
                        var c: Pair;
                        var d: Pair;
                    }

                    fun doTest(): TestResult {
                        var a: Pair;
                        var b: NestedPair;
                        a = Pair(1, 2);
                        b = NestedPair(Pair(1, 2));
                        modifyPointer(&a);
                        modifyNested(&b);
                        return TestResult(
                            modifyLocal(),
                            modifyParameter(Pair(1, 2)),
                            a,
                            b.pair
                        );
                    }

                    fun modifyLocal(): Pair {
                        var a: Pair;
                        a = Pair(1, 2);
                        a.x = 10;
                        a.y = 20;
                        return a;
                    }

                    fun modifyParameter(p: Pair): Pair {
                        p.x = 11;
                        p.y = 21;
                        return p;
                    }

                    fun modifyPointer(p: Pair*): int64 {
                        *p.x = 12;
                        *p.y = 22;
                        return 0;
                    }

                    fun modifyNested(p: NestedPair*): int64 {
                        *p.pair.x = 13;
                        *p.pair.y = 23;
                        return 0;
                    }
                """,
                "doTest()",
                "Class<TestResult>{Class<Pair>{Int64[10], Int64[20]}, Class<Pair>{Int64[11], Int64[21]}, Class<Pair>{Int64[12], Int64[22]}, Class<Pair>{Int64[13], Int64[23]}}"
        )
    }
}