package kz.vasilyev.pawnshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kz.vasilyev.pawnshop.application.AddApplicationScreen
import kz.vasilyev.pawnshop.login.LoginScreen
import kz.vasilyev.pawnshop.main.MainScreen
import kz.vasilyev.pawnshop.registration.RegistrationScreen
import kz.vasilyev.pawnshop.ui.theme.PawnshopTheme
import kz.vasilyev.pawnshop.admin.AdminScreen
import kz.vasilyev.pawnshop.admin.AdminApplicationDetailScreen
import kz.vasilyev.pawnshop.application.UserApplicationDetailScreen
import kz.vasilyev.pawnshop.application.ContactDetailsScreen
import kz.vasilyev.pawnshop.application.ConfirmationScreen
import com.google.firebase.auth.FirebaseAuth
import kz.vasilyev.pawnshop.data.repository.AuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawnshopTheme(darkTheme = false) {
                val navController = rememberNavController()

                var initialRoute by remember { mutableStateOf<String?>(null) }
                val auth = remember { FirebaseAuth.getInstance() }
                val authRepository = remember { AuthRepository() }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val user = authRepository.getCurrentUser()
                        if (user != null) {
                            initialRoute = if (user.admin) "admin_main" else "main"
                        } else {
                            auth.signOut()
                            initialRoute = "login"
                        }
                    } else {
                        initialRoute = "login"
                    }
                }

                if (initialRoute != null) {
                    NavHost(navController = navController, startDestination = initialRoute!!) {
                        composable(
                            "login",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) {
                            LoginScreen(navController = navController)
                        }
                        composable(
                            "registration",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) {
                            RegistrationScreen(navController = navController)
                        }
                        composable(
                            "main",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) {
                            MainScreen(navController = navController)
                        }
                        composable(
                            "add_application",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) {
                            AddApplicationScreen(navController = navController)
                        }
                        composable(
                            "admin_main",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) {
                            AdminScreen(navController = navController)
                        }
                        composable(
                            "admin_application_detail/{applicationId}",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) { backStackEntry ->
                            val applicationId = backStackEntry.arguments?.getString("applicationId")
                            AdminApplicationDetailScreen(navController = navController, applicationId = applicationId)
                        }
                        composable(
                            "user_application_detail/{applicationId}",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) { backStackEntry ->
                            val applicationId = backStackEntry.arguments?.getString("applicationId")
                            UserApplicationDetailScreen(navController = navController, applicationId = applicationId)
                        }
                        composable(
                            "contactDetailsScreen/{applicationId}",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) { backStackEntry ->
                            val applicationId = backStackEntry.arguments?.getString("applicationId")
                            ContactDetailsScreen(navController = navController, applicationId = applicationId)
                        }
                        composable(
                            "confirmation_screen",
                            enterTransition = { fadeIn(animationSpec = tween(0)) },
                            exitTransition = { fadeOut(animationSpec = tween(0)) }
                        ) {
                            ConfirmationScreen(navController = navController)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}