@file:Suppress("FunctionName")

package org.jonnyzzz.bloom

/**
 * The code is based on the public domain code
 * from https://github.com/yonik/java_util
 * by Yonik Seeley, originally created by Austin Appleby (in C++)
 *
 * This file is published under public domain and MIT licenses to allow using it everywhere.
 * Optionally, you may keep the reference to the Kotlin port author - Eugene Petrenko me@jonnyzzz.com
 *
 * Note, this is simplified and patched version of the Murmur3_128 algorithm
 */
object MurmurHash3 {
    @PublishedApi
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun rotateLeft(i: Long, distance: Int): Long = i shl distance or (i ushr -distance)

    @PublishedApi
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun fmix64(k: Long): Long {
        @Suppress("NAME_SHADOWING")
        var k = k
        k = k xor (k ushr 33)
        k *= -0xae502812aa7333L
        k = k xor (k ushr 33)
        k *= -0x3b314601e57a13adL
        k = k xor (k ushr 33)
        return k
    }

    tailrec fun murmurhash3_x64_128(s: String, seed: Int = 0) : Pair<Long, Long> {
        val rem = s.length % 8 //we been 128 padding aka 4 chars / 16 bytes
        //align the string with \0 chars
        if (rem != 0) return murmurhash3_x64_128(s + "\u0000".repeat(rem), seed)
        return murmurhash3_x64_128(seed, s.length / 4, {
            val offset = it * 4
            s[offset + 3].toLong().and(0xffffL).shl(48) or
            s[offset + 2].toLong().and(0xffffL).shl(32) or
            s[offset + 1].toLong().and(0xffffL).shl(16) or
            s[offset + 0].toLong().and(0xffffL)
        }, ::Pair)
    }

    fun murmurhash3_x64_128(buf: ByteArray, seed: Int = 0): Pair<Long, Long> {
        require(buf.size % 16 == 0) { "the buf must be properly padded to 16 bytes / 128 bits, but was ${buf.size}, rem ${buf.size % 16}" }
        return murmurhash3_x64_128(seed, buf.size / 8, {
            val offset = it * 8
            buf[offset + 7].toLong().and(0xffL).shl(56) or
            buf[offset + 6].toLong().and(0xffL).shl(48) or
            buf[offset + 5].toLong().and(0xffL).shl(40) or
            buf[offset + 4].toLong().and(0xffL).shl(32) or
            buf[offset + 3].toLong().and(0xffL).shl(24) or
            buf[offset + 2].toLong().and(0xffL).shl(16) or
            buf[offset + 1].toLong().and(0xffL).shl(8) or
            buf[offset + 0].toLong().and(0xffL)
        }, ::Pair)
    }

    /** Returns the MurmurHash3_x64_128 hash*/
    inline fun <R> murmurhash3_x64_128(seed: Int,
                                       sizeInLongs: Int,
                                       getLongLittleEndian: (longOffset: Int) -> Long,
                                       handleResult: (Long, Long) -> R
    ): R {
        require(sizeInLongs % 2 == 0) { "the input must be properly padded to 2 longs / 128 bites" }

        // The original algorithm does have a 32 bit unsigned seed.
        // We have to mask to match the behavior of the unsigned types and prevent sign extension.
        var h1 = seed.toLong() and 0x00000000FFFFFFFFL
        var h2 = seed.toLong() and 0x00000000FFFFFFFFL
        val c1 = -0x783c846eeebdac2bL
        val c2 = 0x4cf5ad432745937fL

        if (sizeInLongs % 2 != 0) throw RuntimeException("The array must have size mod 16 == 0")

        var i = 0
        while (i < sizeInLongs) {
            var k1 = getLongLittleEndian(i++)
            var k2 = getLongLittleEndian(i++)
            k1 *= c1
            k1 = rotateLeft(k1, 31)
            k1 *= c2
            h1 = h1 xor k1
            h1 = rotateLeft(h1, 27)
            h1 += h2
            h1 = h1 * 5 + 0x52dce729
            k2 *= c2
            k2 = rotateLeft(k2, 33)
            k2 *= c1
            h2 = h2 xor k2
            h2 = rotateLeft(h2, 31)
            h2 += h1
            h2 = h2 * 5 + 0x38495ab5
        }

        h1 = h1 xor (sizeInLongs * 8).toLong()
        h2 = h2 xor (sizeInLongs * 8).toLong()
        h1 += h2
        h2 += h1
        h1 = fmix64(h1)
        h2 = fmix64(h2)
        h1 += h2
        h2 += h1

        return handleResult(h1, h2)
    }
}
