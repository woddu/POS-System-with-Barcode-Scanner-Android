package com.example.firebaseapptest.ui.view.screen

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavBackStackEntry
import com.example.firebaseapptest.ui.event.AppEvent
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import java.util.UUID

@Composable
fun Scanner(backStackEntry: NavBackStackEntry, onEvent: (AppEvent) -> Unit, navigate: () -> Unit){
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

    val activity = context as? Activity
    var isLandscape by remember { mutableStateOf(false) }

    val cameraPermission = Manifest.permission.CAMERA

    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, cameraPermission) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT)
                .show()
            //onBack()
        } else {
            hasPermission.value = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission.value) launcher.launch(cameraPermission)
    }

    if (hasPermission.value) {
        // ✅ Your scanner UI here
        // Force a fresh instance per visit
        val instanceKey = remember(backStackEntry) { UUID.randomUUID().toString() }

        key(instanceKey) {
            // Build a new DecoratedBarcodeView each time
            val scannerView = remember {
                DecoratedBarcodeView(context).apply {
                    barcodeView.decoderFactory = DefaultDecoderFactory(
                        BarcodeFormat.entries.filter { it != BarcodeFormat.QR_CODE }
                    )
                }
            }

            // Your callback
            val onScanned: (String) -> Unit = remember {
                { text ->
                    // Handle result (VM event, navigate, etc.)
                    onEvent(AppEvent.OnBarcodeScanned(text))
                    isLandscape = false
                    navigate()
                }
            }

            // Start/stop camera + decoding on enter/leave
            DisposableEffect(scannerView) {
                val beep = BeepManager(context as Activity?)

                scannerView.decodeContinuous { result ->
                    val text = result.text ?: return@decodeContinuous
                    onScanned(text)
                    beep.playBeepSoundAndVibrate()
                    scannerView.pause()
                }
                scannerView.resume()

                onDispose {
                    scannerView.pause()
                    scannerView.barcodeView.stopDecoding()
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize()
                        .then(
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                Modifier.padding(horizontal = 32.dp)
                            } else {
                                Modifier.padding(vertical = 32.dp)
                            }
                        ),
                    factory = { scannerView }
                )
                IconButton(
                    onClick = {
                        isLandscape = !isLandscape
                        activity?.requestedOrientation =
                            if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .padding(bottom = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0.dp else 32.dp)
                        .size(56.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Rotate", modifier = Modifier.size(32.dp))
                }
            }
        }
    } else {
        // 🚫 Optional fallback UI
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera access is required to scan barcodes.")
        }
    }
}