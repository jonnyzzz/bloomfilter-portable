package org.jonnyzzz.bloom

import org.junit.Assert
import org.junit.Test

class FixedBitSetTest {
    private val sz = 1290

    @Test
    fun testOneBit() {
        repeat(sz) { i ->
            val bs = FixedBitSet(sz)
            (0..sz).forEach { j ->
                Assert.assertFalse(bs.get(j))
            }

            Assert.assertFalse(bs.get(i))
            bs.set(i)
            Assert.assertTrue(bs.get(i))

            (0..sz).filter { it != i }.forEach { j ->
                Assert.assertFalse(bs.get(j))
            }
        }
    }

    @Test
    fun testMergeBits() {
        val bs = FixedBitSet(sz)

        repeat(sz) { i ->
            (0..sz).forEach { j ->
                Assert.assertEquals( j < i, bs.get(j))
            }

            Assert.assertFalse(bs.get(i))
            bs.set(i)
            Assert.assertTrue(bs.get(i))

            (0..sz).forEach { j ->
                Assert.assertEquals( j <= i, bs.get(j))
            }
        }
    }
}

