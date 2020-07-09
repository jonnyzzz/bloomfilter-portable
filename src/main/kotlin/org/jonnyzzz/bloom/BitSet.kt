package org.jonnyzzz.bloom

private const val bitsPerElement = Long.SIZE_BITS

@Suppress("NOTHING_TO_INLINE")
internal class FixedBitSet(sizeInBits: Int) {
    private val data = LongArray(sizeInBits / Long.SIZE_BITS + 2)

    fun get(i: Int): Boolean {
        val offset = i / bitsPerElement
        val rem = i % bitsPerElement
        val pack = data[offset]
        return 1L == (pack shr rem) and 0x1L
    }

    fun set(i: Int) {
        val offset = i / bitsPerElement
        val rem = i % bitsPerElement

        val pack = data[offset]
        data[offset] = pack or (1L shl rem)
    }
}
