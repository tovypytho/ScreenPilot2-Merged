package id.eujian.cbt.screenpilot.flutter

import io.flutter.embedding.engine.plugins.FlutterPlugin

class ScreenPilotPlatformViewPlugin : FlutterPlugin {

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        val context = binding.applicationContext
        val engine = binding.flutterEngine
        binding.platformViewRegistry.registerViewFactory(
            "screenpilot_webview",
            ScreenPilotWebViewFactory(context, engine)
        )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        // Factory lifecycle is bound to the engine; nothing to unregister here.
    }
}
