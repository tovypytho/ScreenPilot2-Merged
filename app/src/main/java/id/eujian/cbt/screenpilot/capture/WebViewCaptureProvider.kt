package id.eujian.cbt.screenpilot.capture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.webkit.WebView
import java.lang.ref.WeakReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebViewCaptureProvider(
    webView: WebView
) : CaptureProvider {

    private val webViewRef = WeakReference(webView)

    override suspend fun capture(): CaptureResult = withContext(Dispatchers.Main.immediate) {
        try {
            val webView = webViewRef.get()
                ?: return@withContext CaptureResult.Error("WebView no longer available")

            val w = webView.width
            val h = webView.height
            if (w <= 0 || h <= 0) {
                return@withContext CaptureResult.Error("WebView dimensions are zero (w=$w, h=$h)")
            }

            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            webView.draw(canvas)
            CaptureResult.Success(bitmap)
        } catch (e: Exception) {
            CaptureResult.Error(e.message ?: "Unknown capture error")
        }
    }
}
