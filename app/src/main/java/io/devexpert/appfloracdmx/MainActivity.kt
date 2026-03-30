package io.devexpert.appfloracdmx
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val auth = FirebaseAuth.getInstance()
            val authManager = AuthManager(auth)
            val navController = rememberNavController()
            var isUserLoggedIn by remember { mutableStateOf(false) }

            // Observa el estado de autenticación y ajusta la ruta inicial
            LaunchedEffect(authManager) {
                isUserLoggedIn = authManager.isUserAuthenticated()
            }

            // Configura la navegación con la ruta inicial en función del estado del usuario
            Navegacion(
                navController = navController,
                authManager = authManager,
                onSuccessLogin = {
                    isUserLoggedIn = true
                    navController.navigate("pantalla_menuFlora") { popUpTo("login_screen") { inclusive = true } }
                },
                startDestination = if (isUserLoggedIn) "pantalla_menuFlora" else "login_screen"
            )
        }
    }
}