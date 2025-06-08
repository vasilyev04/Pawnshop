package kz.vasilyev.pawnshop.admin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kz.vasilyev.pawnshop.data.model.ApplicationStatus
import kz.vasilyev.pawnshop.data.repository.ApplicationRepository
import com.google.firebase.auth.FirebaseAuth
import kz.vasilyev.pawnshop.main.ApplicationItem // Переиспользуем ApplicationItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser
    val applicationRepository = remember { ApplicationRepository() }

    // Если пользователь не авторизован (хотя LoginScreen должен был перенаправить)
    if (currentUser == null) {
        Text("Қате: Пайдаланушы авторизацияланбаған.")
        return
    }

    val tabs = remember { ApplicationStatus.values().toList() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val selectedStatus = tabs[selectedTabIndex]

    // Flow для получения всех заявок (или фильтрованных по статусу)
    val allApplicationsFlow = remember { applicationRepository.getAllApplications() }
    val allApplications by allApplicationsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Админ панелі") }, // Заголовок на казахском
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Шығу") // Иконка выхода
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, status ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(status.toLocalizedAdminString()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Отображение списка заявок для текущего выбранного статуса
            val filteredApplications = allApplications.filter { it.status == selectedStatus }

            if (filteredApplications.isEmpty()) {
                Text(
                    text = "Бұл статуста заявкалар жоқ", // Нет заявок в этом статусе
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(filteredApplications) { application ->
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
                                navController.navigate("admin_application_detail/${application.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

// Расширение для локализации статусов для админки
fun ApplicationStatus.toLocalizedAdminString(): String {
    return when (this) {
        ApplicationStatus.UNDER_REVIEW -> "Қаралуда" // В рассмотрении
        ApplicationStatus.AWAITING_CONFIRMATION -> "Растауды күтуде" // Ожидает подтверждения
        ApplicationStatus.APPROVED -> "Мақұлданды" // Одобрена
        ApplicationStatus.REJECTED -> "Қабылданбады" // Отклонена
    }
}

@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    AdminScreen(rememberNavController())
} 