package kz.vasilyev.pawnshop.application

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import kotlinx.coroutines.coroutineScope
import kz.vasilyev.pawnshop.data.model.Application
import kz.vasilyev.pawnshop.data.model.ApplicationStatus
import kz.vasilyev.pawnshop.data.repository.ApplicationRepository
import kz.vasilyev.pawnshop.admin.toLocalizedAdminString
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserApplicationDetailScreen(navController: NavController, applicationId: String?) {
    val applicationRepository = remember { ApplicationRepository() }
    var application by remember { mutableStateOf<Application?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(applicationId) {
        if (applicationId != null) {
            applicationRepository.getApplicationById(applicationId).collect { fetchedApplication ->
                application = fetchedApplication
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Өтінім", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Артқа", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (application == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(photoBitmaps) { bitmap ->
                                    AsyncImage(
                                        model = bitmap,
                                        contentDescription = "Өтінім фотосы",
                                        modifier = Modifier.size(200.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        } else {
                            Text("Фотосуреттер жоқ", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                }

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
                        val statusColor = when (application?.status) {
                            ApplicationStatus.UNDER_REVIEW, ApplicationStatus.AWAITING_CONFIRMATION -> Color(0xFF2196F3)
                            ApplicationStatus.APPROVED -> Color(0xFF4CAF50)
                            ApplicationStatus.REJECTED -> Color(0xFFF44336)
                            else -> Color.Black
                        }
                        Text(
                            application?.status?.toLocalizedAdminString() ?: "Белгісіз мәртебе",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            application?.category ?: "Санат жоқ",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )

                        application?.timestamp?.let { timestamp ->
                            Text("Тапсыру күні: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(timestamp.toDate())}", fontSize = 14.sp, color = Color.Black)
                        }

                        application?.price?.let { price ->
                            Text("Бағаланған құны: ${String.format("%.2f ₸", price)}", fontSize = 14.sp, color = Color.Black)
                        }

                        application?.adminComment?.let { adminComment ->
                            if (adminComment.isNotBlank()) {
                                Text("Әкімші түсініктемесі: $adminComment", fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (application?.status == ApplicationStatus.AWAITING_CONFIRMATION) {
                    Button(
                        onClick = {
                            application?.id?.let {
                                navController.navigate("contactDetailsScreen/$it")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                    ) {
                        Text("Рәсімдеу", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            application?.let { currentApplication ->
                                coroutineScope.launch {
                                    val updatedApplication = currentApplication.copy(
                                        status = ApplicationStatus.REJECTED
                                    )
                                    val result = applicationRepository.updateApplication(updatedApplication)
                                    if (result.isSuccess) {
                                        navController.popBackStack()
                                    } else {
                                        println("Өтінімді қабылдамау кезінде қате: ${result.exceptionOrNull()?.localizedMessage}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350), contentColor = Color.White)
                    ) {
                        Text("Бас тарту", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserApplicationDetailScreenPreview() {
    UserApplicationDetailScreen(rememberNavController(), "sample_app_id")
}