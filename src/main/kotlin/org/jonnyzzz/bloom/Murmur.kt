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

internal interface MurMur3HashFunction<T : Any> {
    fun <R> hash(t: T, seed: Int, hash: (Long, Long) -> R): R
}

internal val murmurhash3_x64_128_string = object : MurMur3HashFunction<String> {
    override fun <R> hash(t: String, seed: Int, hash: (Long, Long) -> R): R {
        return murmurhash3_x64_128(seed, Char.SIZE_BYTES * t.length, {
            val offset = it / Char.SIZE_BYTES
                    t[offset + 3].toLong().and(0xffffL).shl(48) or
                    t[offset + 2].toLong().and(0xffffL).shl(32) or
                    t[offset + 1].toLong().and(0xffffL).shl(16) or
                    t[offset + 0].toLong().and(0xffffL)
        }, {
            val char = t[it / Char.SIZE_BYTES].toLong()
            if (it % 2 == 1) {
                char.and(0xffL)
            } else {
                char.shl(8).and(0xffL)
            }
        }, hash)
    }
}

internal val murmurhash3_x64_128_bytes = object : MurMur3HashFunction<ByteArray> {
    override fun <R> hash(t: ByteArray, seed: Int, hash: (Long, Long) -> R): R {
        return murmurhash3_x64_128(seed, t.size, {
            val offset = it
                    t[offset + 7].toLong().and(0xffL).shl(56) or
                    t[offset + 6].toLong().and(0xffL).shl(48) or
                    t[offset + 5].toLong().and(0xffL).shl(40) or
                    t[offset + 4].toLong().and(0xffL).shl(32) or
                    t[offset + 3].toLong().and(0xffL).shl(24) or
                    t[offset + 2].toLong().and(0xffL).shl(16) or
                    t[offset + 1].toLong().and(0xffL).shl(8) or
                    t[offset + 0].toLong().and(0xffL)
        }, {
            t[it].toLong().and(0xffL)
        }, hash)
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun rotateLeft(i: Long, distance: Int): Long = i shl distance or (i ushr -distance)

@Suppress("NOTHING_TO_INLINE")
private inline fun fmix64(k: Long): Long {
    @Suppress("NAME_SHADOWING")
    var k = k
    k = k xor (k ushr 33)
    k *= -0xae502812aa7333L
    k = k xor (k ushr 33)
    k *= -0x3b314601e57a13adL
    k = k xor (k ushr 33)
    return k
}

/** Returns the MurmurHash3_x64_128 hash*/
private inline fun <R> murmurhash3_x64_128(seed: Int,
                                           sizeInBytes: Int,
                                           getLongLittleEndian: (byteOffset: Int) -> Long,
                                           getRemainderBytes: (byteOffset: Int) -> Long,
                                           handleResult: (Long, Long) -> R
): R {
    // The original algorithm does have a 32 bit unsigned seed.
    // We have to mask to match the behavior of the unsigned types and prevent sign extension.
    var h1 = seed.toLong() and 0x00000000FFFFFFFFL
    var h2 = seed.toLong() and 0x00000000FFFFFFFFL
    val c1 = -0x783c846eeebdac2bL
    val c2 = 0x4cf5ad432745937fL

    var i = 0
    val rem = sizeInBytes % 16
    val roundedEnd = sizeInBytes - rem
    while (i < roundedEnd) {
        var k1 = getLongLittleEndian(i)
        var k2 = getLongLittleEndian(i + 8)
        i += 16
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

    var k1: Long = 0
    var k2: Long = 0

    if (rem >= 15) k2 = getRemainderBytes(roundedEnd + 14) and 0xffL shl 48
    if (rem >= 14) k2 = k2 or (getRemainderBytes(roundedEnd + 13) and 0xffL shl 40)
    if (rem >= 13) k2 = k2 or (getRemainderBytes(roundedEnd + 12) and 0xffL shl 32)
    if (rem >= 12) k2 = k2 or (getRemainderBytes(roundedEnd + 11) and 0xffL shl 24)
    if (rem >= 11) k2 = k2 or (getRemainderBytes(roundedEnd + 10) and 0xffL shl 16)
    if (rem >= 10) k2 = k2 or (getRemainderBytes(roundedEnd + 9) and 0xffL shl 8)

    if (rem >= 9) {
        k2 = k2 or (getRemainderBytes(roundedEnd + 8) and 0xffL)
        k2 *= c2
        k2 = java.lang.Long.rotateLeft(k2, 33)
        k2 *= c1
        h2 = h2 xor k2
    }

    if (rem >= 8) k1 = getRemainderBytes(roundedEnd + 7) shl 56
    if (rem >= 7) k1 = k1 or (getRemainderBytes(roundedEnd + 6) and 0xffL shl 48)
    if (rem >= 6) k1 = k1 or (getRemainderBytes(roundedEnd + 5) and 0xffL shl 40)
    if (rem >= 5) k1 = k1 or (getRemainderBytes(roundedEnd + 4) and 0xffL shl 32)
    if (rem >= 4) k1 = k1 or (getRemainderBytes(roundedEnd + 3) and 0xffL shl 24)
    if (rem >= 3) k1 = k1 or (getRemainderBytes(roundedEnd + 2) and 0xffL shl 16)
    if (rem >= 2) k1 = k1 or (getRemainderBytes(roundedEnd + 1) and 0xffL shl 8)

    if (rem >= 1) {
        k1 = k1 or (getRemainderBytes(roundedEnd + 0) and 0xffL)
        k1 *= c1
        k1 = rotateLeft(k1, 31)
        k1 *= c2
        h1 = h1 xor k1
    }

    h1 = h1 xor sizeInBytes.toLong()
    h2 = h2 xor sizeInBytes.toLong()
    h1 += h2
    h2 += h1
    h1 = fmix64(h1)
    h2 = fmix64(h2)
    h1 += h2
    h2 += h1

    return handleResult(h1, h2)
}
