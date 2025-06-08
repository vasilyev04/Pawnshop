package kz.vasilyev.pawnshop.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kz.vasilyev.pawnshop.data.repository.ApplicationRepository
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kz.vasilyev.pawnshop.data.model.Application
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import coil3.Bitmap
import kz.vasilyev.pawnshop.data.model.ApplicationStatus
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    val applicationRepository = remember { ApplicationRepository() }

    if (currentUser == null) {
        Text("Пользователь не авторизован")
        return
    }

    val applicationsFlow = remember(currentUser.uid) { applicationRepository.getUserApplications(currentUser.uid) }
    val applications = applicationsFlow.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color(0xFFF0F0F0), // Серый фон для всего экрана
        topBar = {
            TopAppBar(
                title = { Text("Менің заявкаларым") }, // Заголовок на казахском
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F0F0)),
                actions = {
                    IconButton(onClick = {
                        auth.signOut() // Выход из Firebase
                        navController.navigate("login") { // Переход на экран входа
                            popUpTo("main") { inclusive = true } // Удалить главный экран из стека
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp, // Иконка выхода
                            contentDescription = "Шығу", // Описание на казахском
                            tint = Color.Red // Красный цвет иконки
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_application") },
                containerColor = Color(0xFF1F1F1F) // Черный цвет FAB
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Заявка қосу", tint = Color.White)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp), // Добавим небольшой отступ внутри экрана
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (applications.value.isEmpty()) {
                Text("Заявкалар тізімі бос", fontSize = 18.sp)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(applications.value) { application ->
                        // Декодируем фото из Base64 перед передачей в ApplicationItem
                        val decodedBitmaps: List<Bitmap> = remember(application.photoBase64) {
                            application.photoBase64?.mapNotNull { base64String ->
                                try {
                                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                } catch (e: IllegalArgumentException) {
                                    null
                                }
                            } ?: emptyList()
                        }
                        ApplicationItem(
                            application = application,
                            photoBitmaps = decodedBitmaps,
                            onClick = {
                                navController.navigate("user_application_detail/${application.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationItem(
    application: Application,
    photoBitmaps: List<Bitmap>?,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!photoBitmaps.isNullOrEmpty()) {
                    LazyRow(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(photoBitmaps) { bitmap ->
                            AsyncImage(
                                model = bitmap,
                                contentDescription = "Фото заявки",
                                modifier = Modifier.size(60.dp), // Размер миниатюры в слайдере
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    Text("Фото нет", fontSize = 12.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Категория: ${application.category}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("Комментарий: ${application.comment}", maxLines = 2)
                application.timestamp?.let { timestamp ->
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    Text("Дата: ${dateFormat.format(timestamp.toDate())}", fontSize = 12.sp)
                }
            }

            val statusColor = when (application.status) {
                ApplicationStatus.UNDER_REVIEW, ApplicationStatus.AWAITING_CONFIRMATION -> Color(0xFF2196F3)
                ApplicationStatus.APPROVED -> Color(0xFF4CAF50)
                ApplicationStatus.REJECTED -> Color(0xFFF44336)
                else -> Color.Black
            }
            Text(
                when (application.status) {
                    ApplicationStatus.UNDER_REVIEW -> "Қаралуда"
                    ApplicationStatus.AWAITING_CONFIRMATION -> "Растауды күтуде"
                    ApplicationStatus.APPROVED -> "Мақұлданды"
                    ApplicationStatus.REJECTED -> "Қабылданбады"
                },
                fontSize = 14.sp,
                color = statusColor // Применяем динамический цвет
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(rememberNavController())
} 