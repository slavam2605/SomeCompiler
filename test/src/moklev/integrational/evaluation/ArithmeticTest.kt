package moklev.integrational.evaluation

import org.junit.Test
import java.util.*

/**
 * @author Moklev Vyacheslav
 */
class ArithmeticTest(testMode: EvaluationTestMode) : EvaluationTestBase(testMode) {
    companion object {
        const val seed = 7468297228L
    }

    @Test
    fun testSimpleArithmetic() {
        runTest("", "3", "Int64[3]")
        runTest("", "1 + 2", "Int64[3]")
        runTest("", "1 - 2", "Int64[-1]")
        runTest("", "2 * 3", "Int64[6]")
        runTest("", "7 / 3", "Int64[2]")
        runTest("", "2 == 3", "Boolean[false]")
        runTest("", "3 < 4", "Boolean[true]")
        runTest("", "1 - (2 - 3)", "Int64[2]")
    }

    @Test
    fun testRandomArithmetic() {
        val random = Random(seed)
        fun createRandomExpression(depth: Int): Pair<String, Long> {
            if (depth == 0)
                return random.nextInt(1000000000).let { it.toString() to it.toLong() }
            val (left, leftVal) = createRandomExpression(depth - 1)
            val (right, rightVal) = createRandomExpression(depth - 1)
            return when (random.nextInt(4)) {
                0 -> "($left)+($right)" to leftVal + rightVal
                1 -> "($left)-($right)" to leftVal - rightVal
                2 -> "($left)*($right)" to leftVal * rightVal
                3 -> if (rightVal != 0L)
                    "($left)/($right)" to leftVal / rightVal
                else
                    "($left)/(($right)+1)" to leftVal / (rightVal + 1)
                else -> throw RuntimeException("Random value is out of bounds")
            }
        }

        for (iter in 0 until 1000) {
            val (expr, value) = createRandomExpression(10)
            runTest("", expr, "Int64[$value]")
        }
    }
}