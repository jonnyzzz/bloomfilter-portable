package org.jonnyzzz.bloom

/**
 * The base interface for a bloom filter
 */
interface BloomFilter<T : Any> {
    /**
     * checks if a given element in the set.
     * @return true if the [t] is not null and is included in the set. It may return false-positive as well
     */
    fun contains(t: T?) : Boolean
}

object BloomFilters {
    fun trainStringFilter(input: Collection<String>,
                          correctAnswerProbability: Double = 0.95) : BloomFilter<String>
            = stringBloomFilterBuilder(input.toSet(), correctAnswerProbability)

    fun loadStringFilter(byteArray: ByteArray): BloomFilter<String> = TODO()
    fun saveStringFilter(filter: BloomFilter<String>): ByteArray = TODO()
}


