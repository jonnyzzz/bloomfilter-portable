package org.jonnyzzz.bloom

import java.security.SecureRandom

object StringPermutations {
    private val basicChars = """ !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~"""
            .map { it.toString() }
            .shuffled(SecureRandom())
            .toList()

    fun allStringsOfSize(size: Int): Sequence<String> {
        if (size == 0) return emptySequence()
        if (size == 1) return basicChars.shuffled().asSequence()

        return allStringsOfSize(size - 1).flatMap { longEl ->
            allStringsOfSize(1).map { it + longEl }
        }
    }
}