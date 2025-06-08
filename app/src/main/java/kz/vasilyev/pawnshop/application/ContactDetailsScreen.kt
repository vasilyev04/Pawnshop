package kz.vasilyev.pawnshop.application

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import kz.vasilyev.pawnshop.data.repository.ApplicationRepository
import kz.vasilyev.pawnshop.data.model.ApplicationStatus
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import kz.vasilyev.pawnshop.data.model.Application

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailsScreen(navController: NavController, applicationId: String?) {
    var fio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var expandedPaymentMethod by remember { mutableStateOf(false) }

    val paymentMethods = listOf("Қолма-қол ақша", "Картаға аудару")

    val applicationRepository = remember { ApplicationRepository() }
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    var application by remember { mutableStateOf<Application?>(null) }

    LaunchedEffect(applicationId) {
        if (applicationId != null) {
            applicationRepository.getApplicationById(applicationId).collect { fetchedApplication ->
                application = fetchedApplication
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Байланыс деректері", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Артқа", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Аты-жөні", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextField(
                        value = fio,
                        onValueChange = { fio = it },
                        label = { Text("Аты-жөні") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            disabledContainerColor = Color(0xFFF0F0F0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )
                    Text("Телефон", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextField(
                        value = phone,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phone = digitsOnly
                            }
                        },
                        label = { Text("Телефон") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        visualTransformation = PhoneVisualTransformation,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            disabledContainerColor = Color(0xFFF0F0F0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )
                    Text("Мекен-жайы", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Мекен-жайы") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            disabledContainerColor = Color(0xFFF0F0F0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            disabledLabelColor = Color.Gray
                        )
                    )
                    Text("Ақшаны алу тәсілі", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    ExposedDropdownMenuBox(
                        expanded = expandedPaymentMethod,
                        onExpandedChange = { expandedPaymentMethod = !expandedPaymentMethod },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = paymentMethod,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Ақшаны алу тәсілі (қолма-қол ақша, картаға аудару)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaymentMethod) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF0F0F0),
                                unfocusedContainerColor = Color(0xFFF0F0F0),
                                disabledContainerColor = Color(0xFFF0F0F0),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                disabledTextColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedLabelColor = Color.Gray,
                                unfocusedLabelColor = Color.Gray,
                                disabledLabelColor = Color.Gray
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedPaymentMethod,
                            onDismissRequest = { expandedPaymentMethod = false }
                        ) {
                            paymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        paymentMethod = method
                                        expandedPaymentMethod = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (applicationId == null || application == null) {
                            println("Қате: Өтінім ID немесе өтінім жүктелмеген.")
                            return@launch
                        }

                        val updatedApplication = application!!.copy(
                            status = ApplicationStatus.APPROVED,
                            userFio = fio,
                            userPhone = phone,
                            userAddress = address,
                            userPaymentMethod = paymentMethod
                        )
                        val result = applicationRepository.updateApplication(updatedApplication)

                        if (result.isSuccess) {
                            navController.navigate("confirmation_screen") {
                                popUpTo("main") { inclusive = false }
                            }
                        } else {
                            println("Мәртебені жаңарту кезінде қате: ${result.exceptionOrNull()?.localizedMessage}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Растау", fontSize = 18.sp)
            }
        }
    }
}

object PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 10) text.text.substring(0, 10) else text.text
        val out = StringBuilder("+7 (")

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            when (i) {
                2 -> out.append(") ")
                5 -> out.append("-")
                7 -> out.append("-")
            }
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = offset
                if (offset >= 0) transformedOffset += 4
                if (offset >= 3) transformedOffset += 2
                if (offset >= 6) transformedOffset += 1
                if (offset >= 8) transformedOffset += 1
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = offset
                if (offset <= 3) return 0
                if (offset > 3) originalOffset -= 4
                if (offset > 7) originalOffset -= 2
                if (offset > 11) originalOffset -= 1
                if (offset > 14) originalOffset -= 1
                return originalOffset.coerceAtMost(10)
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetTranslator)
    }
} 