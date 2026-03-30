package io.devexpert.appfloracdmx

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloraScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    var isExpanded by remember { mutableStateOf(false) }
    var selectedAlcaldia by remember { mutableStateOf<String?>(null) }
    var floraEspecies by remember { mutableStateOf<List<FloraEspecie>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar datos desde Firestore
    LaunchedEffect(Unit) {
        isLoading = true
        floraEspecies = getFloraEspecies(userId)
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = "EcoFlora Urbana") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE),
                titleContentColor = Color.White
            )
        )

        // Barra de búsqueda con menor altura de texto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Buscar especie...",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp) // Reducir tamaño del texto
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp) // Texto de entrada más pequeño
            )
        }

        Button(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(text = if (isExpanded) "Ocultar Alcaldías" else "Filtrar por Alcaldías")
        }

        if (isExpanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    listOf(
                        "Álvaro Obregón", "Azcapotzalco", "Benito Juárez", "Coyoacán", "Cuajimalpa",
                        "Cuauhtémoc", "Gustavo A. Madero", "Iztacalco", "Iztapalapa", "Magdalena Contreras",
                        "Miguel Hidalgo", "Milpa Alta", "Tláhuac", "Tlalpan", "Venustiano Carranza", "Xochimilco"
                    )
                ) { alcaldia ->
                    Text(
                        text = alcaldia,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedAlcaldia = alcaldia
                                isExpanded = false
                            }
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                }
            }
        }

        Text(
            text = "Alcaldía seleccionada: ${selectedAlcaldia ?: "Ninguna"}",
            modifier = Modifier
                .padding(10.dp)
                .background(
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            val filteredEspecies = floraEspecies
                .filter { especie ->
                    (selectedAlcaldia == null || especie.alcaldia.equals(selectedAlcaldia, ignoreCase = true)) &&
                            (searchQuery.isBlank() || especie.nombre.contains(searchQuery, ignoreCase = true))
                }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .padding(7.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEspecies, key = { it.id }) { especie ->
                    FloraItem(
                        especie = especie,
                        currentUserId = userId,
                        onHeartClick = { especieId, isFavorite, updatedLikeCount ->
                            toggleFavorite(especieId, userId, isFavorite, updatedLikeCount) {
                                floraEspecies = floraEspecies.map { item ->
                                    if (item.id == especieId) item.copy(isFavorite = isFavorite, likeCount = updatedLikeCount) else item
                                }
                            }
                        },
                        navController = navController
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.navigate("pantalla_registroFlora") },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Registrar Flora")
            }
            Button(
                onClick = {
                    if (selectedAlcaldia != null) {
                        selectedAlcaldia = null
                    } else {
                        Toast.makeText(navController.context, "No hay filtros activos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Eliminar Filtro")
            }
        }
    }
}

@Composable
fun FloraItem(
    especie: FloraEspecie,
    currentUserId: String?,
    onHeartClick: (String, Boolean, Int) -> Unit,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp)
            .clickable { navController.navigate("pantalla_detalleFlora/${especie.id}") },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = especie.nombre,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp) // Tamaño aumentado a 20sp
            )
            Text(
                text = especie.alcaldia,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 18.sp) // Tamaño aumentado a 16sp
            )
            // Mostrar imagen de la especie usando Coil con tamaño ajustado
            if (especie.url.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(especie.url),
                    contentDescription = "Imagen de ${especie.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)  // Aumentar la altura de la imagen
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Centrar verticalmente el contenido
        ) {
            IconToggleButton(
                checked = especie.isFavorite,
                onCheckedChange = { isChecked ->
                    val updatedLikeCount = if (isChecked) especie.likeCount + 1 else especie.likeCount - 1
                    onHeartClick(especie.id, isChecked, updatedLikeCount)
                }
            ) {
                Icon(
                    imageVector = if (especie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (especie.isFavorite) Color.Red else Color.Gray
                )
            }
            Text(
                text = "${especie.likeCount} Likes",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

data class FloraEspecie(
    val id: String = "",
    val nombre: String = "",
    val alcaldia: String = "",
    val isFavorite: Boolean = false,
    val likeCount: Int = 0,
    val url: String = "" // Agregar campo para URL de la imagen
)

suspend fun getFloraEspecies(userId: String?): List<FloraEspecie> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val result = db.collection("Flora_datos").get().await()
        result.map { doc ->
            val favorites = doc.get("favorites") as? Map<String, Boolean>
            FloraEspecie(
                id = doc.id,
                nombre = doc.getString("nombreEspecie") ?: "Sin nombre",
                alcaldia = doc.getString("alcaldia") ?: "Sin alcaldía",
                isFavorite = favorites?.get(userId) == true,
                likeCount = doc.getLong("likeCount")?.toInt() ?: 0,
                url = doc.getString("url") ?: "" // Obtener URL de la imagen
            )
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener especies", e)
        emptyList()
    }
}

fun toggleFavorite(
    especieId: String,
    userId: String?,
    isFavorite: Boolean,
    updatedLikeCount: Int,
    onComplete: () -> Unit
) {
    if (userId == null) return

    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("Flora_datos").document(especieId)

    val updates = mutableMapOf<String, Any>(
        "likeCount" to updatedLikeCount
    )

    if (isFavorite) {
        updates["favorites.$userId"] = true
    } else {
        updates["favorites.$userId"] = FieldValue.delete()
    }

    docRef.update(updates)
        .addOnSuccessListener {
            Log.d("Firestore", "Favorite and like count updated successfully")
            onComplete()
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error updating favorite and like count", e)
        }
}

