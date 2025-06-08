package kz.vasilyev.pawnshop.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException // Import specific Firebase exceptions
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch
import kz.vasilyev.pawnshop.R // Assuming R is accessible for resources like logo
import kz.vasilyev.pawnshop.data.repository.AuthRepository

@Composable
fun LoginScreen(navController: NavController) {
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    val authRepository = remember { AuthRepository() }
    val coroutineScope = rememberCoroutineScope()
    var loginError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "✪ e-Ломбард",
            fontSize = 32.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Кіру",
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = emailText,
            onValueChange = { emailText = it },
            label = { Text("Email") }, // Белгі "Email"
            modifier = Modifier.fillMaxWidth(),
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

        TextField(
            value = passwordText,
            onValueChange = { passwordText = it },
            label = { Text("Құпия сөз") }, // Белгі "Пароль" аударылды
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                loginError = null // Алдыңғы қатені тазалау
                coroutineScope.launch {
                    val result = authRepository.loginUser(emailText, passwordText)
                    result.onSuccess {
                        println("DEBUG: User logged in successfully. admin: ${it.admin}")
                        println("DEBUG: User email: ${it.email}")
                        println("DEBUG: User UUID: ${it.uuid}")
                        if (it.admin) {
                            println("DEBUG: Navigating to admin screen")
                            navController.navigate("admin_main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            println("DEBUG: Navigating to main screen")
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }.onFailure {
                        println("DEBUG: Login failed: ${it.message}")
                        println("DEBUG: Exception type: ${it.javaClass.simpleName}")
                        loginError = when (it) {
                            is FirebaseAuthInvalidCredentialsException -> "Электрондық пошта немесе құпия сөз қате"
                            is FirebaseAuthInvalidUserException -> "Мұндай пайдаланушы табылмады"
                            // is FirebaseAuthEmailException -> Handle specific email format errors if needed
                            // is FirebaseAuthPasswordException -> Handle specific password errors if needed
                            else -> "Кіру кезінде белгісіз қате пайда болды. Қайталап көріңіз." // Басқа қателер үшін жалпы хабарлама
                        }
                    }
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
            Text("Кіру", fontSize = 18.sp) // Батырма мәтіні "Войти" аударылды
        }

        loginError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                navController.navigate("registration")
            }
        ) {
            Text("Тіркелу") // Батырма мәтіні "Зарегистрироваться" аударылды
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}

