package kz.vasilyev.pawnshop.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import kz.vasilyev.pawnshop.data.model.Application
import kz.vasilyev.pawnshop.data.model.ApplicationStatus
import kz.vasilyev.pawnshop.data.repository.ApplicationRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationDetailScreen(navController: NavController, applicationId: String?) {
    val applicationRepository = remember { ApplicationRepository() }
    var application by remember { mutableStateOf<Application?>(null) }
    var priceText by remember { mutableStateOf("") }
    var adminCommentText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(applicationId) {
        if (applicationId != null) {
            applicationRepository.getApplicationById(applicationId).collect { fetchedApplication ->
                application = fetchedApplication
                fetchedApplication?.let {
                    priceText = it.price?.toString() ?: ""
                    adminCommentText = it.adminComment ?: ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Өтінім мәліметтері") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Артқа")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F0F0))
            )
        }
    ) { innerPadding ->
        if (application == null) {
            CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val photoBitmaps: List<Bitmap> = remember(application?.photoBase64) {
                    application?.photoBase64?.mapNotNull { base64String ->
                        try {
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    } ?: emptyList()
                }

                if (photoBitmaps.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(photoBitmaps) { bitmap ->
                            AsyncImage(
                                model = bitmap,
                                contentDescription = "Өтінім фотосы",
                                modifier = Modifier
                                    .size(200.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Фотосуреттер жоқ", color = Color.Gray)
                    }
                }

                Text("Санат: ${application?.category}", fontSize = 18.sp)
                Text("Түсініктеме: ${application?.comment}", fontSize = 16.sp)

                if (application?.status == ApplicationStatus.APPROVED) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Клиенттің байланыс деректері:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            application?.userFio?.let {
                                if (it.isNotBlank()) Text("Аты-жөні: $it", fontSize = 16.sp)
                            }
                            application?.userPhone?.let {
                                if (it.isNotBlank()) Text("Телефон: +7 ($it)", fontSize = 16.sp)
                            }
                            application?.userAddress?.let {
                                if (it.isNotBlank()) Text("Мекен-жайы: $it", fontSize = 16.sp)
                            }
                            application?.userPaymentMethod?.let {
                                if (it.isNotBlank()) Text("Ақшаны алу тәсілі: $it", fontSize = 16.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Баға") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = adminCommentText,
                    onValueChange = { adminCommentText = it },
                    label = { Text("Әкімші түсініктемесі") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (applicationId == null) {
                                dialogMessage = "Қате: Өтінім ID табылмады."
                                showDialog = true
                                return@launch
                            }

                            val price = priceText.toDoubleOrNull()
                            if (price == null || price <= 0) {
                                dialogMessage = "Дұрыс баға енгізіңіз."
                                showDialog = true
                                return@launch
                            }
                            if (adminCommentText.isBlank()) {
                                dialogMessage = "Админ түсініктемесін енгізіңіз."
                                showDialog = true
                                return@launch
                            }

                            val updatedApplication = application!!.copy(
                                price = price,
                                adminComment = adminCommentText,
                                status = ApplicationStatus.AWAITING_CONFIRMATION
                            )
                            val result = applicationRepository.updateApplication(updatedApplication)

                            if (result.isSuccess) {
                                dialogMessage = "Бағалау сәтті жіберілді!"
                                navController.popBackStack()
                            } else {
                                dialogMessage = "Бағалау жіберу кезінде қате пайда болды: ${result.exceptionOrNull()?.localizedMessage}"
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
                    Text("Бағалауды клиентке жіберу", fontSize = 18.sp)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Бағалау мәртебесі") },
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
fun AdminApplicationDetailScreenPreview() {
    AdminApplicationDetailScreen(rememberNavController(), "sample_app_id")
}