package kz.vasilyev.pawnshop.application

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.content.Context
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kz.vasilyev.pawnshop.data.repository.AuthRepository
import kz.vasilyev.pawnshop.data.repository.ApplicationRepository
import kz.vasilyev.pawnshop.data.model.Application
import kz.vasilyev.pawnshop.data.model.ApplicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddApplicationScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val categories = listOf(
        "Киім",
        "Техника",
        "Жиһаз",
        "Зергерлік бұйымдар",
        "Антиквариат",
        "Басқа"
    )

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedPhotos = (selectedPhotos + uris).take(5)
    }

    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val applicationRepository = remember { ApplicationRepository() }
    val coroutineScope = rememberCoroutineScope()

    fun urisToBase64(uris: List<Uri>, context: Context): List<String> {
        return uris.mapNotNull { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Өтінім") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Артқа")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .clickable { multiplePhotoPickerLauncher.launch("image/*") }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPhotos.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Add, contentDescription = "Фотосурет қосу")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Фотосуреттерді осы жерге сүйреп әкеліңіз\nнемесе файлды таңдаңыз",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyRow(modifier = Modifier.fillMaxSize()) {
                        items(selectedPhotos) { uri ->
                            Box(modifier = Modifier.size(200.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Таңдалған фото",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { selectedPhotos = selectedPhotos - uri },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Фотосуретті жою")
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (selectedPhotos.size < 5) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .clickable { multiplePhotoPickerLauncher.launch("image/*") }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Фото қосу", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Text("Санат", fontSize = 16.sp)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory ?: "Санатты таңдаңыз",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Санатты таңдаңыз") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Text("Түсініктеме", fontSize = 16.sp)
            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Түсініктеме") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val currentUser = authRepository.getCurrentUser()
                        if (currentUser == null) {
                            dialogMessage = "Қате: Пайдаланушы авторизацияланбаған."
                            showDialog = true
                            return@launch
                        }

                        if (selectedPhotos.isEmpty()) {
                            dialogMessage = "Фотосуреттерді таңдаңыз."
                            showDialog = true
                            return@launch
                        }

                        if (selectedCategory == null) {
                            dialogMessage = "Санатты таңдаңыз."
                            showDialog = true
                            return@launch
                        }

                        if (commentText.isBlank()) {
                            dialogMessage = "Түсініктеме енгізіңіз."
                            showDialog = true
                            return@launch
                        }

                        val photoBase64List = urisToBase64(selectedPhotos, context)

                        if (photoBase64List.isEmpty() && selectedPhotos.isNotEmpty()) {
                            dialogMessage = "Фотосуреттерді өңдеу кезінде қате пайда болды."
                            showDialog = true
                            return@launch
                        }

                        val application = Application(
                            userId = currentUser.uuid,
                            photoBase64 = photoBase64List,
                            category = selectedCategory!!,
                            comment = commentText,
                            status = ApplicationStatus.UNDER_REVIEW
                        )

                        val result = applicationRepository.saveApplication(application)

                        if (result.isSuccess) {
                            dialogMessage = "Өтінім сәтті жіберілді!"
                            navController.popBackStack()
                        } else {
                            dialogMessage = "Өтінімді жіберу кезінде қате пайда болды: ${result.exceptionOrNull()?.localizedMessage}"
                        }
                        showDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F1F1F),
                    contentColor = Color.White
                )
            ) {
                Text("Бағалауға жіберу", fontSize = 18.sp)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Өтінім мәртебесі") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Жарайды")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddApplicationScreenPreview() {
    AddApplicationScreen(rememberNavController())
} 