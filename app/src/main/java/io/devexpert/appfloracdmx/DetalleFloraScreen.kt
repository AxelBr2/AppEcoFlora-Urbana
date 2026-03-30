package io.devexpert.appfloracdmx

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleFloraScreen(id: String, navController: NavHostController) {
    var floraDetalles by remember { mutableStateOf<Map<String, Any>?>(null) }
    var comentarios by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var nuevoComentario by remember { mutableStateOf("") }
    val authManager = AuthManager(FirebaseAuth.getInstance())
    val firestore = FirebaseFirestore.getInstance()

    // Recuperar detalles desde Firestore
    LaunchedEffect(id) {
        firestore.collection("Flora_datos").document(id)
            .get()
            .addOnSuccessListener { doc ->
                floraDetalles = doc.data
                comentarios = doc.data?.get("comentarios") as? List<Map<String, String>> ?: emptyList()
                isLoading = false
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al obtener los detalles", it)
                isLoading = false
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = "Detalles de flora") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE),
                titleContentColor = Color.White
            )
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (floraDetalles != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "${floraDetalles!!["nombreEspecie"]}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Imagen de la especie
                val imageUrl = floraDetalles!!["url"] as? String
                if (!imageUrl.isNullOrEmpty()) {
                    item {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Imagen de la especie",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Información fija
// Información fija
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Título de la sección
                        Text(
                            text = "Información de la especie",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Start
                        )

                        // Detalles con íconos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Descripción",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Descripción: ${floraDetalles!!["descripcion"] ?: "Sin descripción"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFlorist,
                                contentDescription = "Cuidados",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Cuidados: ${floraDetalles!!["cuidados"] ?: "Sin cuidados"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = "Alcaldía",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Alcaldía: ${floraDetalles!!["alcaldia"] ?: "Sin alcaldía"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }


                // Comentarios
                item {
                    Text(
                        text = "Comentarios",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                val comentariosMostrados = if (expanded) comentarios else comentarios.take(3)

                items(comentariosMostrados) { comentario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = comentario["nickname"] ?: "Anónimo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = comentario["texto"] ?: "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Botón para ver más o menos comentarios
                if (comentarios.size > 3) {
                    item {
                        Text(
                            text = if (expanded) "Ver menos comentarios" else "Ver más comentarios",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable { expanded = !expanded }
                        )
                    }
                }

                // Input de nuevo comentario
                item {
                    OutlinedTextField(
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        label = { Text("Escribe tu comentario") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (nuevoComentario.isNotBlank()) {
                                val user = authManager.getCurrentUser()
                                if (user != null) {
                                    // Obtener el nickname del usuario
                                    firestore.collection("users").document(user.uid).get()
                                        .addOnSuccessListener { userDoc ->
                                            val nickname =
                                                userDoc.getString("nickname") ?: "Anónimo"
                                            val nuevo = mapOf(
                                                "nickname" to nickname,
                                                "texto" to nuevoComentario
                                            )
                                            firestore.collection("Flora_datos").document(id)
                                                .update("comentarios", FieldValue.arrayUnion(nuevo))
                                                .addOnSuccessListener {
                                                    comentarios = comentarios + nuevo
                                                    nuevoComentario = ""
                                                }
                                                .addOnFailureListener {
                                                    Log.e(
                                                        "Firestore",
                                                        "Error al agregar comentario",
                                                        it
                                                    )
                                                }
                                        }
                                        .addOnFailureListener {
                                            Log.e("Firestore", "Error al obtener el nickname", it)
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(text = "Enviar Comentario")
                    }
                }

                item {
                    Button(
                        onClick = {
                            val localizacion = floraDetalles?.get("localizacion") as? String
                            if (!localizacion.isNullOrEmpty()) {
                                // Dividir la cadena para obtener latitud y longitud
                                val coordenadas = localizacion.split(",").map { it.trim() }
                                if (coordenadas.size == 2) {
                                    val lat = coordenadas[0].toDoubleOrNull() ?: 0.0
                                    val lng = coordenadas[1].toDoubleOrNull() ?: 0.0
                                    Log.d(
                                        "DetalleFloraScreen",
                                        "Navegando a mapa con lat: $lat, lng: $lng"
                                    )
                                    navController.navigate("mapa/$lat/$lng")
                                } else {
                                    Log.e(
                                        "DetalleFloraScreen",
                                        "Formato de localización incorrecto: $localizacion"
                                    )
                                }
                            } else {
                                Log.e("DetalleFloraScreen", "Localización no encontrada")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, bottom = 28.dp) // Padding extra en la parte inferior
                    ) {
                        Text("Ver ubicación")
                    }
                }
            }
        } else {
            Text(text = "No se encontraron detalles", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

