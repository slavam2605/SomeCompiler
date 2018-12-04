package moklev.integrational.evaluation

import org.junit.Test
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import java.lang.reflect.Method

class EvaluationTestRunner(testClass: Class<*>) : ParentRunner<Pair<Method, EvaluationTestMode>>(testClass) {
    override fun getChildren(): List<Pair<Method, EvaluationTestMode>> {
        val testMethods = testClass.getAnnotatedMethods(Test::class.java).map { it.method }
        return EvaluationTestMode.values().flatMap { testMode ->
            testMethods.map { it to testMode }
        }
    }

    override fun describeChild(methodPair: Pair<Method, EvaluationTestMode>): Description {
        return Description.createTestDescription(
                testClass.javaClass,
                "${methodPair.first.name} [${methodPair.second}]",
                *methodPair.first.annotations
        )
    }

    override fun runChild(methodPair: Pair<Method, EvaluationTestMode>, notifier: RunNotifier?) {
        val description = describeChild(methodPair)
        val eachNotifier = EachTestNotifier(notifier, description)
        eachNotifier.fireTestStarted()
        try {
            methodPair.first.invoke(testClass.onlyConstructor.newInstance(methodPair.second))
        } catch (e: Throwable) {
            eachNotifier.addFailure(e)
        } finally {
            eachNotifier.fireTestFinished()
        }
    }
}

enum class EvaluationTestMode {
    Evaluation, X86Compilation
}