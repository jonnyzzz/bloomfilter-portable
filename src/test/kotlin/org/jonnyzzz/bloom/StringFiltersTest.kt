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
}


object StringPermutations {
    private val cache = mutableMapOf<Int, List<String>>()
    private val basicChars = """ !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~"""
            .map { it.toString() }
            .toList()

    fun allStringsOfSize(size: Int): List<String> {
        if (size == 0) return listOf()
        if (size == 1) return basicChars

        return cache.computeIfAbsent(size) {
            val base = allStringsOfSize(size - 1)
            val result = base.toMutableList()

            for (ch in basicChars) {
                result += base.map { ch + it }
            }

            result
        }
    }


}
