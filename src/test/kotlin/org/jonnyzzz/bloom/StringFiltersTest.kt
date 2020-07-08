package org.jonnyzzz.bloom

import org.junit.Assert
import org.junit.Test

class StringFiltersTest {
    @Test
    fun testEmpty() {
        val empty = BloomFilters.trainStringFilter(listOf())
        StringPermutations.allStringsOfSize(2).forEach {
            Assert.assertFalse("should not contain - $it", empty.contains(it))
        }
    }

    @Test
    fun testSimple() {
        val expectedP = 0.95
        val input = setOf("a", "b", "c")
        val filter = BloomFilters.trainStringFilter(input, expectedP)

        for (s in input) {
            Assert.assertTrue("must contains - $s", filter.contains(s))
        }

        var count = 0
        var error = 0

        StringPermutations.allStringsOfSize(3).forEach {
            if (it !in input) {
                count++
                if (filter.contains(it)) error++
            }
        }

        val actualP = 1 - error.toDouble() / count
        println("There are $error errors for $count tries: P(correctAnswer) = $actualP")
        Assert.assertTrue(actualP >= expectedP)
    }
}

