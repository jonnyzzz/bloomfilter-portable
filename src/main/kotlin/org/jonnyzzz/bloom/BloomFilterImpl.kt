@file:Suppress("FunctionName")

package org.jonnyzzz.bloom

import java.util.*
import kotlin.math.ceil
import kotlin.math.ln

internal fun stringBloomFilterBuilder(source: Set<String>,
                                      correctAnswerProbability: Double
) = bloomFilterBuilder<String>(source, correctAnswerProbability) { item, seed, hash ->
    MurmurHash3.murmurhash3_x64_128(item, seed, hash)
}

internal inline fun <reified T : Any> bloomFilterBuilder(source: Set<T>,
                                                         correctAnswerProbability: Double,
                                                         crossinline murmur: (T, seed: Int, hash:(Long, Long) -> Unit) -> Unit
): BloomFilter<T> {
    /**
     * Computes the expected size of the bloom filter array for the given
     * probability. The formula assumes properties for the hash functions
     * to achieve good results.
     *
     * See https://en.wikipedia.org/wiki/Bloom_filter#Probability_of_false_positives
     */
    val size = source.size
    val p = correctAnswerProbability.coerceIn(0.0001, 0.995)

    val ln2 = ln(2.0)
    val numberOfBits = (-size * ln(1.0 - p) / ln2 * ln2).toInt().coerceAtLeast(2)
    val numberOfHashFunctions = ceil(ln2 * size / numberOfBits).toInt().coerceAtLeast(1)

    require(numberOfBits < 24) { "Too many bits: $numberOfBits. Try lowering the correct answer probability parameter, which is $correctAnswerProbability" }

    //how many functions fit into 128 bit mur-mur
    val functionsPerMur3 = (Long.SIZE_BITS / numberOfBits) * 2

    if (numberOfHashFunctions < functionsPerMur3) {
        return bloomFilterBuilder_singleMur(source, numberOfBits, numberOfHashFunctions, murmur)
    }

    TODO("Not implemented for the case where more than one mur3 hash is needed")
}

private inline fun <reified T : Any> bloomFilterBuilder_singleMur(source: Collection<T>,
                                                                  numberOfBits: Int,
                                                                  numberOfHashFunctions: Int,
                                                                  crossinline murmur: (T, seed: Int, hash: (Long, Long) -> Unit) -> Unit
): BloomFilter<T> {
    val state = BitSet(numberOfBits)
    source.forEach { item ->
        murmur(item, 0) { h1, h2 ->
            processBits(h1, h2, numberOfBits, numberOfHashFunctions) { state.set(it) }
        }
    }

    return object: BloomFilter<T> {
        override fun contains(t: T?): Boolean {
            if (t == null) return false

            var result = true
            murmur(t, 0) { h1, h2 ->
                processBits(h1, h2, numberOfBits, numberOfHashFunctions) {
                    if (!state.get(it)) {
                        result = false
                        return@processBits
                    }
                }
            }

            return result
        }
    }
}

/**
 * executes bit callbacks,
 * returns the the updated [count] parameter
 */
@Suppress("NAME_SHADOWING")
private inline fun processBits(t: Long,
                               bitsPerBlock: Int,
                               count: Int,
                               action: (bit: Int) -> Unit): Int {
    val mask = (1L shl bitsPerBlock) - 1

    var t = t
    var count = count
    var off = 0
    while(off + bitsPerBlock < Long.SIZE_BITS && count > 0) {
        action((t and mask).toInt())
        off += bitsPerBlock
        count--
        t = t.shl(bitsPerBlock)
    }
    return count
}

private inline fun processBits(h1: Long,
                               h2: Long,
                               numberOfBits: Int,
                               numberOfHashFunctions: Int,
                               action: (bit: Int) -> Unit) {
    val rest = processBits(h1, numberOfBits, numberOfHashFunctions, action)
    val final = processBits(h2, numberOfBits, rest, action)
    require(final == 0)
}
