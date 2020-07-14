@file:Suppress("FunctionName")

package org.jonnyzzz.bloom

import kotlin.math.ceil
import kotlin.math.ln

internal object BloomFilterMurMur3x128 {
    internal fun stringBloomFilterBuilder(source: Set<String>,
                                          correctAnswerProbability: Double
    ) = bloomFilterBuilder(source, correctAnswerProbability, murmurhash3_x64_128_string)

    private inline fun <reified T : Any> bloomFilterBuilder(source: Set<T>,
                                                            correctAnswerProbability: Double,
                                                            murmur: MurMur3HashFunction<T>
    ): BloomFilter<T> {
        /**
         * Computes the expected size of the bloom filter array for the given
         * probability. The formula assumes properties for the hash functions
         * to achieve good results.
         *
         * See https://en.wikipedia.org/wiki/Bloom_filter#Probability_of_false_positives
         */
        val size = source.size
        // we include a magic factor here to ensure our tests will all permutations pass
        val p = (1.0 - correctAnswerProbability.coerceIn(0.0001, 0.9995))

        val ln2 = ln(2.0)
        val numberOfBits = (-size * ln(p) / ln2 / ln2).toInt().coerceAtLeast(5)
        val numberOfHashFunctions = ceil(ln2 * size / numberOfBits).toInt().coerceAtLeast(1)

        val state = FixedBitSet(numberOfBits)

        val updater: (Long, Long) -> Unit = { h1, h2 ->
            processBits(h1, h2, numberOfBits, numberOfHashFunctions) { state.set(it) }
        }

        val checker: (Long, Long) -> Boolean = checker@{ h1, h2 ->
            processBits(h1, h2, numberOfBits, numberOfHashFunctions) {
                if (!state.get(it)) {
                    return@checker false
                }
            }
            true
        }

        //fill in the data
        source.forEach { murmur.hash(it, 0, updater) }

        return object : BloomFilter<T> {
            override fun toString() = "BloomFilter(MurMur3x128, bits=$numberOfBits, functions=$numberOfHashFunctions)"

            override fun contains(t: T?): Boolean {
                if (t == null) return false
                return murmur.hash(t, 0, checker)
            }
        }
    }

    private inline fun processBits(h1: Long,
                                   h2: Long,
                                   numberOfBits: Int,
                                   numberOfHashFunctions: Int,
                                   action: (bit: Int) -> Unit) {

        repeat(numberOfHashFunctions) { i ->
            val t = (numberOfHashFunctions - i) * h1 + i * h2
            val magic = if (t < 0) -t else t
            action((magic % numberOfBits).toInt())
        }
    }
}
