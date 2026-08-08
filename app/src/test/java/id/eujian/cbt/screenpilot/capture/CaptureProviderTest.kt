package id.eujian.cbt.screenpilot.capture

import android.graphics.Color
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FakeCaptureProviderTest {

    @Test
    fun `capture returns Success with correct dimensions`() = runBlocking {
        val width = 200
        val height = 150
        val provider = FakeCaptureProvider(width = width, height = height, color = Color.RED)

        val result = provider.capture()

        assertTrue("Expected CaptureResult.Success, got ${result::class.simpleName}", result is CaptureResult.Success)
        val bitmap = (result as CaptureResult.Success).bitmap
        assertEquals(width, bitmap.width)
        assertEquals(height, bitmap.height)
    }

    @Test
    fun `capture returns Success with default dimensions`() = runBlocking {
        val provider = FakeCaptureProvider()

        val result = provider.capture()

        assertTrue("Expected CaptureResult.Success, got ${result::class.simpleName}", result is CaptureResult.Success)
        val bitmap = (result as CaptureResult.Success).bitmap
        assertEquals(100, bitmap.width)
        assertEquals(100, bitmap.height)
    }
}
