package org.jonnyzzz.bloom

import org.junit.Assert
import org.junit.Test
import java.security.SecureRandom

class StringFiltersTest {
    @Test
    fun testEmpty() {
        val empty = BloomFilters.trainStringFilter(listOf())
        StringPermutations.allStringsOfSize(2).forEach {
            Assert.assertFalse("should not contain - $it", empty.contains(it))
        }
    }

    @Test
    fun testSimple() = doExperiment(input = listOf("a", "b", "c"), testSet = StringPermutations.allStringsOfSize(3))

    @Test
    fun test_100() = doExperiment(inputSize = 100, testSetPermDeep = 3)

    @Test
    fun test_1000() = doExperiment(inputSize = 1000, testSetPermDeep = 3)

    @Test
    fun test_10000() = doExperiment(inputSize = 10000, testSetPermDeep = 3)

    private fun doExperiment(expectedP: Double = 0.95,
                             actualExpectedP: Double? = null,
                             inputSize: Int,
                             testSetPermDeep: Int
    ) {
        val testSet = StringPermutations.allStringsOfSize(testSetPermDeep)
        val input = testSet.distinct().take(inputSize).toList().shuffled(SecureRandom())
        require(input.size == inputSize) { "Too few elements in the testSet sequence"}
        doExperiment(expectedP = expectedP, actualExpectedP = actualExpectedP, input = input, testSet = testSet)
    }

    private fun doExperiment(expectedP: Double = 0.95,
                             actualExpectedP: Double? = null,
                             input: Collection<String>,
                             testSet: Sequence<String>
    ) {

        @Suppress("NAME_SHADOWING")
        val actualExpectedP = actualExpectedP ?: (expectedP - 0.1)

        val filter = BloomFilters.trainStringFilter(input, expectedP)
        for (s in input) {
            Assert.assertTrue("must contains - $s", filter.contains(s))
        }

        @Suppress("NAME_SHADOWING")
        val input = input.toSet()

        var count = 0
        var error = 0

        testSet.forEach {
            if (it !in input) {
                count++
                if (filter.contains(it)) error++
            }
        }

        require(count > input.size * 2) { "There must be enough entries, but were train set = ${input.size}, all set = ${count}"}
        val computedP = 1.0 - error.toDouble() / count
        println("There are $error errors for $count tries: P(correctAnswer) = $computedP, expected $actualExpectedP")
        Assert.assertTrue("computedP=$computedP >= actualExpectedP=$actualExpectedP", computedP >= actualExpectedP - 1e-4)
    }
}
