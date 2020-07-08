package org.jonnyzzz.bloom

object StringPermutations {
    private val cache = mutableMapOf<Int, List<String>>()
    private val basicChars = """ !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~"""
            .map { it.toString() }
            .toList()

    fun allStringsOfSize(size: Int): List<String> {
        if (size == 0) return listOf()
        if (size == 1) return basicChars

        val cached= cache[size]
        if (cached != null) return cached

        val base = allStringsOfSize(size - 1)
        val result = base.toList().toMutableList()

        for (ch in basicChars) {
            result += base.map { ch + it }
        }

        cache[size] = result.toList()
        return result
    }
}