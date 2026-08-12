package id.eujian.cbt.screenpilot.flutter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.webkit.WebViewClient
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import id.eujian.cbt.screenpilot.capture.CaptureProviderRegistration
import id.eujian.cbt.screenpilot.capture.CaptureProviderRegistry
import id.eujian.cbt.screenpilot.capture.WebViewCaptureProvider

/**
 * Project-owned Flutter PlatformView that hosts a WebView rendering the
 * `capture_test_b.html` debug page. The WebView is registered as a capture
 * provider ONLY once it is genuinely ready: the expected B page has finished
 * loading AND the WebView reports non-zero width/height. This mirrors the
 * readiness gate already used for the internal (A) provider and keeps the
 * owner-aware registry lifecycle safe (A registered -> B becomes ready and
 * registers -> registry resolves B -> B dispose closes only B -> registry
 * restores still-live A).
 */
class ScreenPilotWebViewFactory(
    private val context: Context,
    private val engine: FlutterEngine
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        return ScreenPilotWebViewPlatformView(context)
    }

    private class ScreenPilotWebViewPlatformView(
        private val context: Context
    ) : PlatformView {

        private val webView: WebView
        private var registration: CaptureProviderRegistration? = null
        private var disposed = false
        private var pageFinished = false
        private val mainHandler = Handler(Looper.getMainLooper())

        private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            maybeRegisterProvider()
        }

        init {
            webView = WebView(context).apply {
                settings.javaScriptEnabled = false
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // Only the expected B page can promote this PlatformView to a
                        // capture provider. Intermediate/blank callbacks (e.g. about:blank)
                        // must never register.
                        if (url == CAPTURE_TEST_B_URL) {
                            pageFinished = true
                            // Re-check after the layout pass for the freshly loaded page.
                            mainHandler.post { maybeRegisterProvider() }
                        }
                    }
                }
            }
            // Observe layout passes so a non-zero size arriving after page load still
            // triggers registration. This does not give up after a single zero-size check.
            webView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
            webView.loadUrl(CAPTURE_TEST_B_URL)
        }

        override fun getView(): View = webView

        /**
         * Registers the WebView capture provider exactly once, only when all readiness
         * conditions hold and the PlatformView has not been disposed.
         */
        private fun maybeRegisterProvider() {
            if (disposed) return
            if (registration != null) return
            if (!pageFinished) return
            val w = webView.width
            val h = webView.height
            if (w <= 0 || h <= 0) return

            registration = CaptureProviderRegistry.register(WebViewCaptureProvider(webView))
            // Registration is one-shot; stop observing layout once it succeeds.
            safeRemoveLayoutListener()
        }

        override fun dispose() {
            // Mark disposed first so any late WebView/observer callback can never register
            // a provider during or after teardown (including a possible about:blank callback).
            disposed = true
            registration?.close()
            registration = null
            safeRemoveLayoutListener()
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
        }

        private fun safeRemoveLayoutListener() {
            try {
                webView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            } catch (_: Exception) {
                // ViewTreeObserver may be detached; safe to ignore during teardown.
            }
        }

        companion object {
            private const val CAPTURE_TEST_B_URL = "file:///android_asset/capture_test_b.html"
        }
    }
}
