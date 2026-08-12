package id.eujian.cbt.screenpilot.capture

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
 *
 * The cache-file path returned through the MethodChannel stays a normal
 * filesystem path so the existing Flutter `Image.file(File(path))` preview keeps
 * working unchanged. A successful capture is additionally published to
 * Pictures/ScreenPilotDebug via [DebugCaptureExporter] for runtime diagnostics.
 */
object CaptureBridge {

    const val CHANNEL_NAME = "id.eujian.cbt.screenpilot/capture"

    private const val METHOD_CAPTURE = "capture"
    private const val TAG = "CaptureBridge"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainHandler = Handler(Looper.getMainLooper())

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
            showToast(context, "Capture provider unavailable")
            Log.w(TAG, "capture() requested but no provider is registered")
            result.success(mapOf("ok" to false, "error" to "provider_unavailable"))
            return
        }
        scope.launch {
            when (val captureResult = provider.capture()) {
                is CaptureResult.Success -> {
                    val bitmap = captureResult.bitmap
                    val exported = withContext(Dispatchers.IO) {
                        exportPng(context, bitmap)
                    }
                    if (exported == null) {
                        Log.e(TAG, "cache PNG export failed")
                        showToast(context, "PNG export failed — see log")
                        result.success(mapOf("ok" to false, "error" to "png_export_failed"))
                        return@launch
                    }

                    // Side-channel diagnostic copy to Pictures/ScreenPilotDebug.
                    // This never changes the cache-file path returned to Flutter.
                    when (val debugResult = DebugCaptureExporter.savePng(context, bitmap)) {
                        is DebugCaptureExporter.Result.Success -> {
                            Log.i(
                                TAG,
                                "Debug capture saved to Pictures/ScreenPilotDebug: " +
                                    "uri=${debugResult.uri}, displayName=${debugResult.displayName}"
                            )
                            showToast(context, "Debug capture saved to Pictures/ScreenPilotDebug")
                        }
                        is DebugCaptureExporter.Result.Failure -> {
                            Log.e(TAG, "Debug capture export failed: ${debugResult.message}")
                            showToast(context, "Debug capture export failed — see log")
                        }
                        is DebugCaptureExporter.Result.UnsupportedApi -> {
                            Log.w(
                                TAG,
                                "Public debug export to Pictures/ScreenPilotDebug requires " +
                                    "Android 10+ (API 29); cached capture still returned"
                            )
                        }
                    }

                    result.success(
                        mapOf(
                            "ok" to true,
                            "path" to exported.absolutePath,
                            "width" to bitmap.width,
                            "height" to bitmap.height
                        )
                    )
                }
                is CaptureResult.Error -> {
                    Log.w(TAG, "capture error: ${captureResult.message}")
                    showToast(context, "Capture error: ${captureResult.message}")
                    result.success(mapOf("ok" to false, "error" to captureResult.message))
                }
                is CaptureResult.Denied -> {
                    Log.w(TAG, "capture denied")
                    showToast(context, "Capture denied")
                    result.success(mapOf("ok" to false, "error" to "capture_denied"))
                }
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        // The bridge handler and coroutine scope both run on the main thread,
        // but post through the main Looper to guarantee a main-thread Toast.
        mainHandler.post { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
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
