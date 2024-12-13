package com.example.kotlin_project_10

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageDownloaderApp()
        }
    }
}

@Composable
fun ImageDownloaderApp() {
    val networkDispatcher = remember { newSingleThreadContext("Network") }
    val diskDispatcher = remember { newSingleThreadContext("Disk") }

    var imageUrl by remember { mutableStateOf(TextFieldValue("") ) }
    var images by remember { mutableStateOf(listOf<Bitmap>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BasicTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                .padding(8.dp),
            decorationBox = { innerTextField ->
                if (imageUrl.text.isEmpty()) {
                    Text(text = "Введите ссылку...", color = Color.Gray)
                }
                innerTextField()
            }
        )

        Button(onClick = {
            val url = imageUrl.text
            if (url.isNotBlank()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val bitmap = withContext(networkDispatcher) { downloadImage(url) }
                    if (bitmap != null) {
                        images = images + bitmap
                        withContext(diskDispatcher) { saveImage(bitmap) }
                    }
                }
            }
        }) {
            Text("Загрузить изображение")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(images) { image ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Загруженное изображение",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

fun downloadImage(url: String): Bitmap? {
    return try {
        val inputStream = URL(url).openStream()
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}

fun saveImage(bitmap: Bitmap) {
    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val file = File(documentsDir, "downloaded_image_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { fos ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    }
}
