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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.Exception

@Composable
fun QRCodeScanner(
    height: Int? = null,
    width: Int? = null,
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
    val configuration = LocalConfiguration.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .height(height?.dp ?: configuration.screenHeightDp.dp)
            .width(width?.dp ?: configuration.screenWidthDp.dp)
    ) {
        if (hasReadCode) {
            qrCode?.let {
                onScanSuccess(it)
            } ?: run {
                onScanError(scanError!!)
            }
            cameraProvider.unbindAll()
        } else {
            val a = LocalConfiguration.current
            cameraProvider.unbindAll()
            AndroidView(
                factory = { context ->
                    val previewSize = Size(
                        width ?: a.screenWidthDp,
                        height ?: a.screenHeightDp)
                    val previewView = PreviewView(context)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(
                            previewSize
                        )
                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    imageAnalysis.setAnalyzer(
                        executor,
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
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector as CameraSelector,
                                imageAnalysis,
                                preview
                            )
                        }, executor
                    )
                    preview = Preview
                        .Builder()
                        .setTargetResolution(previewSize)
                        .build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    previewView
                },
                modifier = Modifier
                    .border(borderWidth, borderColor)
                    .fillMaxSize()
            )
        }
    }
}
