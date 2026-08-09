package id.eujian.cbt.screenpilot.capture

import android.content.Context
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Phase 3.2 MethodChannel bridge: lets the Flutter test host request a capture
 * from the currently registered capture provider and receive the path of the
 * exported PNG written to the app cache directory.
 */
object CaptureBridge {

    const val CHANNEL_NAME = "id.eujian.cbt.screenpilot/capture"

    private const val METHOD_CAPTURE = "capture"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun setup(flutterEngine: FlutterEngine, context: Context) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_NAME)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    METHOD_CAPTURE -> handleCapture(call, result, context)
                    else -> result.notImplemented()
                }
            }
    }

    private fun handleCapture(call: MethodCall, result: MethodChannel.Result, context: Context) {
        val provider = CaptureProviderRegistry.get()
        if (provider == null) {
            result.success(mapOf("ok" to false, "error" to "provider_unavailable"))
            return
        }
        scope.launch {
            when (val captureResult = provider.capture()) {
                is CaptureResult.Success -> {
                    val exported = withContext(Dispatchers.IO) {
                        exportPng(context, captureResult.bitmap)
                    }
                    if (exported == null) {
                        result.success(mapOf("ok" to false, "error" to "png_export_failed"))
                    } else {
                        result.success(
                            mapOf(
                                "ok" to true,
                                "path" to exported.absolutePath,
                                "width" to captureResult.bitmap.width,
                                "height" to captureResult.bitmap.height
                            )
                        )
                    }
                }
                is CaptureResult.Error -> {
                    result.success(mapOf("ok" to false, "error" to captureResult.message))
                }
                is CaptureResult.Denied -> {
                    result.success(mapOf("ok" to false, "error" to "capture_denied"))
                }
            }
        }
    }

    private fun exportPng(context: Context, bitmap: android.graphics.Bitmap): File? {
        val fileName = "capture_${
            SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        }.png"
        val target = File(context.cacheDir, fileName)
        return try {
            val output = FileOutputStream(target)
            output.use { stream ->
                if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)) {
                    return null
                }
                stream.flush()
            }
            target
        } catch (e: Exception) {
            target.delete()
            null
        }
    }
}
