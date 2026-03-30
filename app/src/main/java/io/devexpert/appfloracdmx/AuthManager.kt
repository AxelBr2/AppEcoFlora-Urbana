package io.devexpert.appfloracdmx
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager(private val auth: FirebaseAuth) {

    private val firestore = FirebaseFirestore.getInstance()

    // Inicia sesión con correo y contraseña
    suspend fun signIn(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            null 
        }
    }

    // Obtiene el usuario actual autenticado
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Registra un nuevo usuario y guarda el apodo y el correo en Firestore
    suspend fun signUp(email: String, password: String, nickname: String): FirebaseUser? {
        return try {
            if (isNicknameTaken(nickname)) {
                throw Exception("El apodo ya está registrado")
            }
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                saveUserToFirestore(user.uid, email, nickname)
            }
            user
        } catch (e: Exception) {
            null // Manejo de error
        }
    }

    // Verifica si un apodo ya está registrado
    suspend fun isNicknameTaken(nickname: String): Boolean {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false // Si hay un error, asumimos que no está tomado (depuración opcional)
        }
    }

    // Guarda la información del usuario en Firestore
    private suspend fun saveUserToFirestore(userId: String, email: String, nickname: String) {
        try {
            val userData = hashMapOf(
                "email" to email,
                "nickname" to nickname
            )
            firestore.collection("users").document(userId).set(userData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Verifica si hay un usuario autenticado
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

}