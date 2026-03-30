package io.devexpert.appfloracdmx

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    var selectedAlcaldia by remember { mutableStateOf<String?>(null) }
    var floraEspecies by remember { mutableStateOf<List<FloraEspecie>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Lista de alcaldías para el filtro horizontal
    val alcaldias = listOf(
        "Álvaro Obregón", "Azcapotzalco", "Benito Juárez", "Coyoacán", "Cuajimalpa",
        "Cuauhtémoc", "Gustavo A. Madero", "Iztacalco", "Iztapalapa", "Magdalena Contreras",
        "Miguel Hidalgo", "Milpa Alta", "Tláhuac", "Tlalpan", "Venustiano Carranza", "Xochimilco"
    )

    LaunchedEffect(Unit) {
        isLoading = true
        floraEspecies = getFloraEspecies(userId)
        isLoading = false
    }

    Scaffold(
        containerColor = colorResource(id = R.color.black_2),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🌿",
                            fontSize = 25.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "EcoFlora Urbana",
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.green_esmerald)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.black_2),
                    scrolledContainerColor = colorResource(id = R.color.black_2)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("pantalla_registroFlora") },
                containerColor = colorResource(id = R.color.green_esmerald),
                contentColor = colorResource(id = R.color.black_2),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar Flora")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 🔍 Barra de Búsqueda Orgánica
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text("Buscar especie...", color = colorResource(id = R.color.secondary_text))
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = colorResource(id = R.color.secondary_text))
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.green_esmerald),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = colorResource(id = R.color.gray_antracita),
                    unfocusedContainerColor = colorResource(id = R.color.gray_antracita),
                    focusedTextColor = colorResource(id = R.color.principal_text),
                    unfocusedTextColor = colorResource(id = R.color.principal_text),
                    cursorColor = colorResource(id = R.color.green_esmerald)
                )
            )

            // Filtro Horizontal de Alcaldías (Chips)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alcaldias) { alcaldia ->
                    val isSelected = selectedAlcaldia == alcaldia
                    Surface(
                        modifier = Modifier.clickable {
                            // Si toca la misma alcaldía, se elimina el filtro
                            selectedAlcaldia = if (isSelected) null else alcaldia
                        },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) colorResource(id = R.color.violet_electric) else colorResource(id = R.color.gray_antracita),
                        contentColor = if (isSelected) colorResource(id = R.color.principal_text) else colorResource(id = R.color.secondary_text)
                    ) {
                        Text(
                            text = alcaldia,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }

            // Contenido de la Lista
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green_esmerald))
                }
            } else {
                val filteredEspecies = floraEspecies.filter { especie ->
                    (selectedAlcaldia == null || especie.alcaldia.equals(selectedAlcaldia, ignoreCase = true)) &&
                            (searchQuery.isBlank() || especie.nombre.contains(searchQuery, ignoreCase = true))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Reemplaza el height fijo, adaptándose a cualquier pantalla
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("pantalla_detalleFlora/${especie.id}") },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.gray_antracita))
    ) {
        Column {
            // Imagen de portada con recorte elegante
            if (especie.url.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(especie.url),
                    contentDescription = "Imagen de ${especie.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop // Evita que las imágenes se aplasten
                )
            } else {
                // Placeholder oscuro por si no hay imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(colorResource(id = R.color.black_2).copy(alpha = 0.5f))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = especie.nombre,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.principal_text)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = especie.alcaldia,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colorResource(id = R.color.violet_electric)
                        )
                    )
                }

                // Sección del Like
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
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
                            contentDescription = "Me gusta",
                            tint = if (especie.isFavorite) Color(0xFFE74C3C) else colorResource(id = R.color.secondary_text)
                        )
                    }
                    Text(
                        text = "${especie.likeCount}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colorResource(id = R.color.secondary_text),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
data class FloraEspecie(
    val id: String = "",
    val nombre: String = "",
    val alcaldia: String = "",
    val isFavorite: Boolean = false,
    val likeCount: Int = 0,
    val url: String = ""
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
                url = doc.getString("url") ?: ""
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