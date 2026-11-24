package com.nuvio.app.features.auth

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * A small QR code encoder (byte mode, error correction level M, versions 1-10), written for the
 * Nuvio link sign-in so no outside library is needed on Android or iOS (owner, 2026-11-24).
 * It follows ISO/IEC 18004: Reed-Solomon over GF(256), the standard module placement and the
 * mask with the lowest penalty score.
 */
internal object QrEncoder {
    // Error correction level M, versions 1-10: EC codewords per block and the data codewords of
    // each block (group 1 blocks, then group 2 blocks).
    private val ecPerBlock = intArrayOf(10, 16, 26, 18, 24, 16, 18, 22, 22, 26)
    private val blocks: List<IntArray> = listOf(
        intArrayOf(16),
        intArrayOf(28),
        intArrayOf(44),
        intArrayOf(32, 32),
        intArrayOf(43, 43),
        intArrayOf(27, 27, 27, 27),
        intArrayOf(31, 31, 31, 31),
        intArrayOf(38, 38, 39, 39),
        intArrayOf(36, 36, 36, 37, 37),
        intArrayOf(43, 43, 43, 43, 44),
    )
    private val alignment: List<IntArray> = listOf(
        intArrayOf(),
        intArrayOf(6, 18),
        intArrayOf(6, 22),
        intArrayOf(6, 26),
        intArrayOf(6, 30),
        intArrayOf(6, 34),
        intArrayOf(6, 22, 38),
        intArrayOf(6, 24, 42),
        intArrayOf(6, 26, 46),
        intArrayOf(6, 28, 50),
    )

    /** The QR modules (true = dark), or null when the text is too long for version 10-M. */
    fun encode(text: String): Array<BooleanArray>? {
        val data = text.encodeToByteArray()
        val version = (1..10).firstOrNull { v ->
            val capacityBits = blocks[v - 1].sum() * 8
            4 + countBits(v) + data.size * 8 <= capacityBits
        } ?: return null
        val codewords = dataCodewords(version, data)
        val all = interleaveWithEc(version, codewords)
        val size = 17 + 4 * version
        var best: Array<BooleanArray>? = null
        var bestPenalty = Int.MAX_VALUE
        for (mask in 0 until 8) {
            val grid = Array(size) { BooleanArray(size) }
            val reserved = Array(size) { BooleanArray(size) }
            drawFunctionPatterns(version, grid, reserved)
            placeData(all, grid, reserved)
            applyMask(mask, grid, reserved)
            drawFormat(mask, grid)
            if (version >= 7) drawVersion(version, grid)
            val penalty = penalty(grid)
            if (penalty < bestPenalty) {
                bestPenalty = penalty
                best = grid
            }
        }
        return best
    }

    private fun countBits(version: Int) = if (version <= 9) 8 else 16

    private fun dataCodewords(version: Int, data: ByteArray): IntArray {
        val capacity = blocks[version - 1].sum()
        val bits = ArrayList<Boolean>()
        fun put(value: Int, length: Int) {
            for (i in length - 1 downTo 0) bits += (value ushr i) and 1 == 1
        }
        put(0b0100, 4)
        put(data.size, countBits(version))
        data.forEach { put(it.toInt() and 0xFF, 8) }
        put(0, minOf(4, capacity * 8 - bits.size))
        while (bits.size % 8 != 0) bits += false
        val bytes = IntArray(capacity)
        var index = 0
        while (index * 8 < bits.size) {
            var b = 0
            for (i in 0 until 8) b = (b shl 1) or (if (bits[index * 8 + i]) 1 else 0)
            bytes[index] = b
            index++
        }
        var pad = 0
        while (index < capacity) {
            bytes[index++] = if (pad++ % 2 == 0) 0xEC else 0x11
        }
        return bytes
    }

    private fun interleaveWithEc(version: Int, data: IntArray): IntArray {
        val sizes = blocks[version - 1]
        val ecLen = ecPerBlock[version - 1]
        val generator = rsGenerator(ecLen)
        val dataBlocks = ArrayList<IntArray>()
        val ecBlocks = ArrayList<IntArray>()
        var offset = 0
        for (size in sizes) {
            val block = data.copyOfRange(offset, offset + size)
            offset += size
            dataBlocks += block
            ecBlocks += rsRemainder(block, generator)
        }
        val result = ArrayList<Int>()
        val maxData = sizes.max()
        for (i in 0 until maxData) dataBlocks.forEach { if (i < it.size) result += it[i] }
        for (i in 0 until ecLen) ecBlocks.forEach { result += it[i] }
        return result.toIntArray()
    }

    private fun gfMultiply(x: Int, y: Int): Int {
        var z = 0
        for (i in 7 downTo 0) {
            z = (z shl 1) xor ((z ushr 7) * 0x11D)
            z = z xor (((y ushr i) and 1) * x)
        }
        return z and 0xFF
    }

    private fun rsGenerator(degree: Int): IntArray {
        val result = IntArray(degree)
        result[degree - 1] = 1
        var root = 1
        repeat(degree) {
            for (j in 0 until degree) {
                result[j] = gfMultiply(result[j], root)
                if (j + 1 < degree) result[j] = result[j] xor result[j + 1]
            }
            root = gfMultiply(root, 0x02)
        }
        return result
    }

    private fun rsRemainder(data: IntArray, generator: IntArray): IntArray {
        val result = IntArray(generator.size)
        for (b in data) {
            val factor = b xor result[0]
            for (i in 0 until result.size - 1) result[i] = result[i + 1]
            result[result.size - 1] = 0
            for (i in result.indices) result[i] = result[i] xor gfMultiply(generator[i], factor)
        }
        return result
    }

    private fun drawFunctionPatterns(version: Int, grid: Array<BooleanArray>, reserved: Array<BooleanArray>) {
        val size = grid.size
        fun set(x: Int, y: Int, dark: Boolean) {
            grid[y][x] = dark
            reserved[y][x] = true
        }
        for (i in 0 until size) {
            set(6, i, i % 2 == 0)
            set(i, 6, i % 2 == 0)
        }
        fun finder(cx: Int, cy: Int) {
            for (dy in -4..4) for (dx in -4..4) {
                val x = cx + dx
                val y = cy + dy
                if (x !in 0 until size || y !in 0 until size) continue
                val d = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy))
                set(x, y, d != 2 && d != 4)
            }
        }
        finder(3, 3)
        finder(size - 4, 3)
        finder(3, size - 4)
        val positions = alignment[version - 1]
        for (i in positions.indices) for (j in positions.indices) {
            val first = i == 0 && j == 0
            val topRight = i == 0 && j == positions.size - 1
            val bottomLeft = i == positions.size - 1 && j == 0
            if (first || topRight || bottomLeft) continue
            for (dy in -2..2) for (dx in -2..2) {
                set(positions[i] + dx, positions[j] + dy, maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy)) != 1)
            }
        }
        // Reserve the format and version areas; drawFormat/drawVersion fill them in.
        for (i in 0..8) {
            reserved[8][i] = true
            reserved[i][8] = true
        }
        for (i in 0 until 8) {
            reserved[8][size - 1 - i] = true
            reserved[size - 1 - i][8] = true
        }
        set(8, size - 8, true)
        if (version >= 7) {
            for (i in 0 until 6) for (j in 0 until 3) {
                reserved[i][size - 11 + j] = true
                reserved[size - 11 + j][i] = true
            }
        }
    }

    private fun placeData(data: IntArray, grid: Array<BooleanArray>, reserved: Array<BooleanArray>) {
        val size = grid.size
        var bitIndex = 0
        val totalBits = data.size * 8
        var right = size - 1
        while (right >= 1) {
            if (right == 6) right = 5
            for (vert in 0 until size) {
                for (j in 0 until 2) {
                    val x = right - j
                    val upward = ((right + 1) and 2) == 0
                    val y = if (upward) size - 1 - vert else vert
                    if (!reserved[y][x] && bitIndex < totalBits) {
                        grid[y][x] = (data[bitIndex ushr 3] ushr (7 - (bitIndex and 7))) and 1 == 1
                        bitIndex++
                    }
                }
            }
            right -= 2
        }
    }

    private fun applyMask(mask: Int, grid: Array<BooleanArray>, reserved: Array<BooleanArray>) {
        val size = grid.size
        for (y in 0 until size) for (x in 0 until size) {
            if (reserved[y][x]) continue
            val invert = when (mask) {
                0 -> (x + y) % 2 == 0
                1 -> y % 2 == 0
                2 -> x % 3 == 0
                3 -> (x + y) % 3 == 0
                4 -> (x / 3 + y / 2) % 2 == 0
                5 -> x * y % 2 + x * y % 3 == 0
                6 -> (x * y % 2 + x * y % 3) % 2 == 0
                else -> ((x + y) % 2 + x * y % 3) % 2 == 0
            }
            if (invert) grid[y][x] = !grid[y][x]
        }
    }

    private fun drawFormat(mask: Int, grid: Array<BooleanArray>) {
        val size = grid.size
        val data = (0 shl 3) or mask // level M = 00
        var rem = data
        repeat(10) { rem = (rem shl 1) xor ((rem ushr 9) * 0x537) }
        val bits = ((data shl 10) or rem) xor 0x5412
        fun bit(i: Int) = (bits ushr i) and 1 == 1
        for (i in 0..5) grid[i][8] = bit(i)
        grid[7][8] = bit(6)
        grid[8][8] = bit(7)
        grid[8][7] = bit(8)
        for (i in 9 until 15) grid[8][14 - i] = bit(i)
        for (i in 0 until 8) grid[8][size - 1 - i] = bit(i)
        for (i in 8 until 15) grid[size - 15 + i][8] = bit(i)
        grid[size - 8][8] = true
    }

    private fun drawVersion(version: Int, grid: Array<BooleanArray>) {
        val size = grid.size
        var rem = version
        repeat(12) { rem = (rem shl 1) xor ((rem ushr 11) * 0x1F25) }
        val bits = (version shl 12) or rem
        for (i in 0 until 18) {
            val dark = (bits ushr i) and 1 == 1
            val a = size - 11 + i % 3
            val b = i / 3
            grid[b][a] = dark
            grid[a][b] = dark
        }
    }

    private fun penalty(grid: Array<BooleanArray>): Int {
        val size = grid.size
        var result = 0
        // Rule 1: runs of five or more in a row or column.
        for (y in 0 until size) {
            var runColor = grid[y][0]
            var run = 1
            for (x in 1 until size) {
                if (grid[y][x] == runColor) {
                    run++
                    if (run == 5) result += 3 else if (run > 5) result++
                } else {
                    runColor = grid[y][x]
                    run = 1
                }
            }
        }
        for (x in 0 until size) {
            var runColor = grid[0][x]
            var run = 1
            for (y in 1 until size) {
                if (grid[y][x] == runColor) {
                    run++
                    if (run == 5) result += 3 else if (run > 5) result++
                } else {
                    runColor = grid[y][x]
                    run = 1
                }
            }
        }
        // Rule 2: 2x2 blocks of one colour.
        for (y in 0 until size - 1) for (x in 0 until size - 1) {
            val c = grid[y][x]
            if (c == grid[y][x + 1] && c == grid[y + 1][x] && c == grid[y + 1][x + 1]) result += 3
        }
        // Rule 3: finder-like patterns.
        val pattern1 = booleanArrayOf(true, false, true, true, true, false, true, false, false, false, false)
        val pattern2 = booleanArrayOf(false, false, false, false, true, false, true, true, true, false, true)
        for (y in 0 until size) for (x in 0..size - 11) {
            if ((0 until 11).all { grid[y][x + it] == pattern1[it] }) result += 40
            if ((0 until 11).all { grid[y][x + it] == pattern2[it] }) result += 40
        }
        for (x in 0 until size) for (y in 0..size - 11) {
            if ((0 until 11).all { grid[y + it][x] == pattern1[it] }) result += 40
            if ((0 until 11).all { grid[y + it][x] == pattern2[it] }) result += 40
        }
        // Rule 4: balance of dark and light.
        val dark = grid.sumOf { row -> row.count { it } }
        val total = size * size
        val k = (kotlin.math.abs(dark * 20 - total * 10) + total - 1) / total - 1
        result += k * 10
        return result
    }
}

/** Draws [text] as a QR code: black modules on white with a quiet zone of four modules. */
@Composable
internal fun QrCodeImage(text: String, modifier: Modifier = Modifier) {
    val modules = remember(text) { QrEncoder.encode(text) } ?: return
    Canvas(modifier = modifier) {
        val count = modules.size + 8
        val cell = size.minDimension / count
        drawRect(Color.White, size = size)
        for (y in modules.indices) for (x in modules.indices) {
            if (modules[y][x]) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset((x + 4) * cell, (y + 4) * cell),
                    size = Size(cell + 0.5f, cell + 0.5f),
                )
            }
        }
    }
}
