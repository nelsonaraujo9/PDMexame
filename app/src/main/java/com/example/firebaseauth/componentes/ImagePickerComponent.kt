package com.example.firebaseauth.ui

import AuthViewModel
import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.firebaseauth.R
import com.example.firebaseauth.RetroFit.ImgurAuthManager
import com.example.firebaseauth.RetroFit.ImgurRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.PermissionStatus.Denied
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePickerComponent(
    onUrlSaved: (String) -> Unit,
    userImgURL: MutableState<String>
) {
    val context = LocalContext.current
    val authManager = ImgurAuthManager()
    val imgurRepository = ImgurRepository(authManager)
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    var imgURL by remember { mutableStateOf<Uri?>(null) }

    // Criar um seletor de imagem
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imgURL = it
            } ?: Toast.makeText(context, "No image was selected", Toast.LENGTH_SHORT).show()
        }
    )

    // Função para fazer upload da imagem
    suspend fun uploadImage(imageUri: Uri) {
        val imageBytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
        if (imageBytes != null) {
            val url = imgurRepository.uploadImage(imageBytes)
            if (url != null) {
                onUrlSaved(url)
                Toast.makeText(context, "Image uploaded with success!", Toast.LENGTH_SHORT).show()
                Log.d("ImagePickerComponent", "Image URL saved: $url")
            } else {
                Toast.makeText(context, "Fail to upload the image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Fail to read image", Toast.LENGTH_SHORT).show()
        }
    }

    // Permissão para acessar o armazenamento
    when (permissionState.status) {
        PermissionStatus.Granted -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                if (userImgURL.value.isNotEmpty()) {
                    AsyncImage(
                        model = userImgURL.value,
                        contentDescription = "User Image",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable { launcher.launch("image/*") }
                    ) {
                        // Imagem decorativa
                        Image(
                            painter = painterResource(id = R.drawable.default_profile),
                            contentDescription = "Decorative Background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(Color.Gray)
                        )
                    }
                }
            }
            LaunchedEffect(imgURL) {
                imgURL?.let {
                    uploadImage(it)
                }
            }
        }
        is Denied -> {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Permission Denied.")
                Text(
                    "Click here to grant access",
                    modifier = Modifier.clickable { permissionState.launchPermissionRequest() }
                )
            }
        }
    }
}
