package id.eujian.cbt.screenpilot.capture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diagnostic export used by the project-owned internal WebView test harness.
 *
 * This does not capture the device display and does not request broad storage
 * access. On Android 10+ it writes a PNG through scoped-storage MediaStore to
 * Pictures/ScreenPilotDebug so runtime smoke tests can visually prove that the
 * WebView provider returned the expected test page.
 */
object DebugCaptureExporter {
    const val OUTPUT_RELATIVE_PATH = "Pictures/ScreenPilotDebug"

    sealed class Result {
        data class Success(val uri: Uri, val displayName: String) : Result()
        data object UnsupportedApi : Result()
        data class Failure(val message: String) : Result()
    }

    suspend fun savePng(context: Context, bitmap: Bitmap): Result = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Do not add legacy WRITE_EXTERNAL_STORAGE solely for a debug harness.
            return@withContext Result.UnsupportedApi
        }

        val displayName = "capture_test_${
            SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        }.png"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/ScreenPilotDebug"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = try {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            return@withContext Result.Failure(
                "MediaStore insert failed: ${e.message ?: e.javaClass.simpleName}"
            )
        } ?: return@withContext Result.Failure("MediaStore insert returned null")

        try {
            val stream = resolver.openOutputStream(uri)
                ?: throw IOException("MediaStore output stream was null")
            stream.use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw IOException("PNG compression failed")
                }
                output.flush()
            }

            val finalizedRows = resolver.update(
                uri,
                ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                },
                null,
                null
            )
            if (finalizedRows <= 0) {
                throw IOException("MediaStore finalization updated no rows")
            }

            Result.Success(uri, displayName)
        } catch (e: Exception) {
            try {
                resolver.delete(uri, null, null)
            } catch (_: Exception) {
                // Best-effort cleanup of an incomplete debug export.
            }
            Result.Failure(e.message ?: e.javaClass.simpleName)
        }
    }
}
