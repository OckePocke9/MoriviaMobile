package com.nuvio.app.features.auth

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class QrEncoderTest {
    private val samples = listOf(
        "https://nuvio.tv/link?code=ABCD-1234",
        "HELLO",
        "https://nuvio.tv/link?code=QWERTY-987654&source=norivia-tablet-sign-in",
        "https://example.com/" + "a".repeat(60),
        "x".repeat(150),
        "Räksmörgås ✓ 日本",
    )

    @Test
    fun everySampleIsReadBackByZxing() {
        samples.forEach { text ->
            val grid = assertNotNull(QrEncoder.encode(text), text)
            assertEquals(text, decode(grid), "decoded text for a ${grid.size}x${grid.size} code")
        }
    }

    @Test
    fun picksTheSmallestVersionAndRejectsTooLongText() {
        assertEquals(21, QrEncoder.encode("HELLO")?.size)
        assertEquals(29, QrEncoder.encode("https://nuvio.tv/link?code=ABCD-1234")?.size)
        assertNull(QrEncoder.encode("x".repeat(300)))
    }

    private fun decode(grid: Array<BooleanArray>): String {
        val scale = 6
        val side = (grid.size + 8) * scale
        val pixels = IntArray(side * side) { 0xFFFFFFFF.toInt() }
        for (y in grid.indices) for (x in grid.indices) {
            if (!grid[y][x]) continue
            for (dy in 0 until scale) for (dx in 0 until scale) {
                pixels[((y + 4) * scale + dy) * side + (x + 4) * scale + dx] = 0xFF000000.toInt()
            }
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))
        return QRCodeReader().decode(bitmap, mapOf(DecodeHintType.PURE_BARCODE to true, DecodeHintType.CHARACTER_SET to "UTF-8")).text
    }
}
