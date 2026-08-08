package id.eujian.cbt.screenpilot.capture

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DebugCaptureExporterTest {

    @Test
    fun `api 28 does not request legacy public storage export`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)

        val result = DebugCaptureExporter.savePng(context, bitmap)

        assertTrue(result is DebugCaptureExporter.Result.UnsupportedApi)
        bitmap.recycle()
    }
}
