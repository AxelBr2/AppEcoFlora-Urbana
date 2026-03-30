package io.devexpert.appfloracdmx
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun Navegacion(
    navController: NavHostController,
    authManager: AuthManager,
    onSuccessLogin: () -> Unit,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = "login_screen" // Define la pantalla inicial dinámicamente
    ) {
        composable("login_screen") {
            LoginScreen(authManager, navController, onSuccessLogin)
        }
        composable("pantalla_recuperar") {
            PantallaRecuperar(navController)
        }
        composable("pantalla_menuFlora") {
            FloraScreen(navController)
        }
        composable("pantalla_registroFlora") {
            FloraRegistrationScreen(navController)
        }
        // Pantalla para mostrar los detalles de una especie
        composable(
            route = "pantalla_detalleFlora/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetalleFloraScreen(id,navController)
        }
        composable(
            "mapa/{lat}/{lng}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            MapaScreen(lat = lat, lng = lng)
        }
    }
}


