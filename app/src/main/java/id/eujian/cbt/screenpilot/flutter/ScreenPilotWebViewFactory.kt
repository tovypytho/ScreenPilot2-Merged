package id.eujian.cbt.screenpilot.flutter

import android.content.Context
import android.view.View
import android.webkit.WebView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import id.eujian.cbt.screenpilot.capture.CaptureProviderRegistration
import id.eujian.cbt.screenpilot.capture.CaptureProviderRegistry
import id.eujian.cbt.screenpilot.capture.WebViewCaptureProvider

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
        private val registration: CaptureProviderRegistration

        init {
            webView = WebView(context).apply {
                settings.javaScriptEnabled = false
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }
            registration = CaptureProviderRegistry.register(WebViewCaptureProvider(webView))
            webView.loadUrl("file:///android_asset/capture_test_b.html")
        }

        override fun getView(): View = webView

        override fun dispose() {
            registration.close()
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
        }
    }
}
