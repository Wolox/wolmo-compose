package ar.com.wolox.wolmo.compose.core.qr

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.Exception

@Composable
fun QRCodeScanner(
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    onScanSuccess: @Composable (String) -> Unit,
    onScanError: @Composable (Exception) -> Unit
) {
    var qrCode by remember { mutableStateOf<String?>(null) }
    var scanError by remember { mutableStateOf<Exception?>(null) }
    var hasReadCode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var cameraSelector by remember { mutableStateOf<CameraSelector?>(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    val lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val focusRequester = remember { FocusRequester() }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (hasReadCode) {
            qrCode?.let {
                onScanSuccess(it)
            } ?: run {
                onScanError(scanError!!)
            }
            cameraProvider.unbindAll()
        } else {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(
                            Size(
                                previewView.width,
                                previewView.height
                            )
                        )
                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QRCodeAnalyzer(
                            onCodeScanned = { result ->
                                qrCode = result
                                hasReadCode = true
                            },
                            onScanError = { e ->
                                scanError = e
                                hasReadCode = true
                            }
                        )
                    )
                    cameraProviderFuture.addListener(
                        {
                            cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector as CameraSelector,
                                imageAnalysis,
                                preview
                            )
                        }, executor
                    )
                    preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    previewView
                },
                modifier = Modifier.border(borderWidth, borderColor).focusRequester(focusRequester)
            )
        }
    }
}
