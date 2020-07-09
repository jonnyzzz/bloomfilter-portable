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
                             inputSize: Int,
                             testSetPermDeep: Int
    ) {
        val testSet = StringPermutations.allStringsOfSize(testSetPermDeep)
        val input = testSet.distinct().take(inputSize).toList().shuffled(SecureRandom())
        require(input.size == inputSize) { "Too few elements in the testSet sequence"}
        doExperiment(expectedP, input, testSet)
    }

    private fun doExperiment(expectedP: Double = 0.95,
                             input: Collection<String>,
                             testSet: Sequence<String>
    ) {
        val filter = BloomFilters.trainStringFilter(input, expectedP)
        for (s in input) {
            Assert.assertTrue("must contains - $s", filter.contains(s))
        }

        var count = 0
        var error = 0

        testSet.forEach {
            if (it !in input) {
                count++
                if (filter.contains(it)) error++
            }
        }

        val actualP = 1.0 - error.toDouble() / count
        println("There are $error errors for $count tries: P(correctAnswer) = $actualP")
        Assert.assertTrue(actualP >= expectedP)
    }
}
