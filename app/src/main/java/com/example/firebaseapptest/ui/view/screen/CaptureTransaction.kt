package com.example.firebaseapptest.ui.view.screen

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun CaptureTransactionAndCrop (
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // Crop launcher (using uCrop)
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let {
                    onEvent(AppEvent.OnImageCropped(it))
                }
            }
        }
    )

    fun launchCropper(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val intent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .getIntent(context)
        cropLauncher.launch(intent)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->

            if (!success) {
                onEvent(AppEvent.OnImageUriChanged(null))
            } else {
                launchCropper(state.imageUri!!)
            }
        }
    )



    fun launchCamera() {
        val photoFile = createTempImageFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "com.example.firebaseapptest.provider",
            photoFile
        )
        onEvent(AppEvent.OnTempImageFileCreated(photoFile))
        onEvent(AppEvent.OnImageUriChanged(uri))
        cameraLauncher.launch(uri)
    }



    if (state.imageUri == null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(onClick = {
                onEvent(AppEvent.OnChoosePaymentMethod("Cash"))
                onEvent(AppEvent.OnAddSale(context))
                navController.navigate(Route.Home.path)
            }) {
                Text("Cash")
            }
            Button(onClick = {
                onEvent(AppEvent.OnChoosePaymentMethod("GCash"))
                launchCamera()
            }) {
                Text("GCash")
            }
            Button(onClick = {
                onEvent(AppEvent.OnChoosePaymentMethod("Cash&GCash"))
                launchCamera()
            }) {
                Text("Cash & GCash")
            }
            Button(onClick = {
                onEvent(AppEvent.OnChoosePaymentMethod("Salmon"))
                launchCamera()
            }) {
                Text("Salmon")
            }
        }
//            LaunchedEffect(state.imageUri == null){
//                launchCamera()
//            }
    } else {
        // Show preview + options
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            val imageFileExists = remember(state.imageUri) {
                state.imageUri.path?.let { File(it).exists() } ?: false
            }
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.9f)
                    .padding(14.dp)
            ) {
                item {
                    Text(
                        text = state.paymentMethod,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                if (imageFileExists) {
                    item {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(state.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Captured image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                val items = aggregateItemsWithPrice(state.itemsInCounter)

                if (state.itemsInCounter.isEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Scan Item(s)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            )
                        }
                    }
                } else {
                    items(items, key = { item -> item.item.code }) { item ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp)
                        ) {
                            Text(
                                text = item.item.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 170.dp),
                                maxLines = 1
                            )
                            Text(
                                text = "₱ ${item.item.price} * ${item.quantity}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!state.itemsInCounter.isEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            )
                            Text(
                                text = "₱ ${state.itemsInCounterTotalPrice}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {

                FilledIconButton(
                    onClick = {
                        onEvent(AppEvent.OnImageUriChanged(null))
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Button(onClick = { launchCamera() }) {
                    Text("Retake")
                }

                Button(onClick = { launchCropper(state.imageUri) }) {
                    Text("Crop")
                }

                if (state.isImageCropped) {
                    Button(onClick = {
                        onEvent(AppEvent.OnAddSale(context))
                        navController.navigate(Route.Home.path)
                    }) {
                        Text("Finish")
                    }
                }

            }
        }
    }
}


fun createTempImageFile(context: Context): File {
    val timestamp = System.currentTimeMillis()
    return File(context.cacheDir, "sale_$timestamp.jpg")
}