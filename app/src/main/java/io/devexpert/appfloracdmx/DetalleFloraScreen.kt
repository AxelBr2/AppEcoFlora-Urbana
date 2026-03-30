package io.devexpert.appfloracdmx

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Asegúrate de importar tu clase R (ej. import com.tu.paquete.R)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleFloraScreen(id: String, navController: NavHostController) {
    var floraDetalles by remember { mutableStateOf<Map<String, Any>?>(null) }
    var comentarios by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var nuevoComentario by remember { mutableStateOf("") }

    val authManager = remember { AuthManager(FirebaseAuth.getInstance()) }
    val firestore = FirebaseFirestore.getInstance()

    // Carga inicial de datos
    LaunchedEffect(id) {
        firestore.collection("Flora_datos").document(id)
            .get()
            .addOnSuccessListener { doc ->
                floraDetalles = doc.data
                // Asegurarnos de que el cast sea seguro
                val rawComentarios = doc.data?.get("comentarios") as? List<*>
                comentarios = rawComentarios?.mapNotNull { it as? Map<String, String> } ?: emptyList()
                isLoading = false
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al obtener los detalles", it)
                isLoading = false
            }
    }

    Scaffold(
        containerColor = colorResource(id = R.color.black_2),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌿", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = "Detalles Botánicos",
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.green_esmerald)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorResource(id = R.color.principal_text)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.black_2),
                    scrolledContainerColor = colorResource(id = R.color.black_2)
                )
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorResource(id = R.color.green_esmerald))
            }
        } else if (floraDetalles != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //IMAGEN PRINCIPAL
                val imageUrl = floraDetalles!!["url"] as? String
                item {
                    if (!imageUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Imagen de la especie",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop // Crop para mantener proporción
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(colorResource(id = R.color.gray_antracita)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalFlorist,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colorResource(id = R.color.secondary_text)
                            )
                        }
                    }
                }

                // Titulo y botón de ubicación
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${floraDetalles!!["nombreEspecie"] ?: "Especie Desconocida"}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.principal_text)
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón de ubicación estilo "Pill"
                        Button(
                            onClick = {
                                val localizacion = floraDetalles?.get("localizacion") as? String
                                if (!localizacion.isNullOrEmpty()) {
                                    val coordenadas = localizacion.split(",").map { it.trim() }
                                    if (coordenadas.size == 2) {
                                        val lat = coordenadas[0].toDoubleOrNull() ?: 0.0
                                        val lng = coordenadas[1].toDoubleOrNull() ?: 0.0
                                        navController.navigate("mapa/$lat/$lng")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.violet_electric),
                                contentColor = colorResource(id = R.color.principal_text)
                            )
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver ubicación en mapa")
                        }
                    }
                }

                // Tarjeta de información
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.gray_antracita)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            InfoRow(
                                icon = Icons.Default.Info,
                                title = "Descripción",
                                content = floraDetalles!!["descripcion"] as? String ?: "Sin descripción"
                            )
                            Divider(color = colorResource(id = R.color.black_2).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))

                            InfoRow(
                                icon = Icons.Default.LocalFlorist,
                                title = "Cuidados",
                                content = floraDetalles!!["cuidados"] as? String ?: "Sin cuidados registrados"
                            )
                            Divider(color = colorResource(id = R.color.black_2).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))

                            InfoRow(
                                icon = Icons.Default.LocationCity,
                                title = "Alcaldía",
                                content = floraDetalles!!["alcaldia"] as? String ?: "No especificada"
                            )
                        }
                    }
                }

                // Sección de comentarios
                item {
                    Text(
                        text = "Comentarios (${comentarios.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.green_esmerald)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Start
                    )
                }

                val comentariosMostrados = if (expanded) comentarios else comentarios.take(3)

                items(comentariosMostrados) { comentario ->
                    // Diseño tipo "Burbuja de chat" moderna
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar simulado
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.violet_electric).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comentario["nickname"]?.take(1)?.uppercase() ?: "A",
                                color = colorResource(id = R.color.violet_electric),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = colorResource(id = R.color.gray_antracita),
                                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = comentario["nickname"] ?: "Anónimo",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.secondary_text)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = comentario["texto"] ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colorResource(id = R.color.principal_text)
                                )
                            )
                        }
                    }
                }

                // Expansor de comentarios
                if (comentarios.size > 3) {
                    item {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(
                                text = if (expanded) "Ocultar comentarios" else "Ver todos los comentarios",
                                color = colorResource(id = R.color.secondary_text)
                            )
                        }
                    }
                }

                // Añadir nuevo comentario
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoComentario,
                            onValueChange = { nuevoComentario = it },
                            placeholder = { Text("Añade un comentario...", color = colorResource(id = R.color.secondary_text)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(id = R.color.green_esmerald),
                                unfocusedBorderColor = colorResource(id = R.color.gray_antracita),
                                focusedContainerColor = colorResource(id = R.color.gray_antracita),
                                unfocusedContainerColor = colorResource(id = R.color.black_2),
                                focusedTextColor = colorResource(id = R.color.principal_text),
                                unfocusedTextColor = colorResource(id = R.color.principal_text)
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón de enviar redondo
                        IconButton(
                            onClick = {
                                if (nuevoComentario.isNotBlank()) {
                                    val user = authManager.getCurrentUser()
                                    if (user != null) {
                                        firestore.collection("users").document(user.uid).get()
                                            .addOnSuccessListener { userDoc ->
                                                val nickname = userDoc.getString("nickname") ?: "Anónimo"
                                                val nuevo = mapOf("nickname" to nickname, "texto" to nuevoComentario)
                                                firestore.collection("Flora_datos").document(id)
                                                    .update("comentarios", FieldValue.arrayUnion(nuevo))
                                                    .addOnSuccessListener {
                                                        comentarios = comentarios + nuevo
                                                        nuevoComentario = ""
                                                    }
                                            }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(colorResource(id = R.color.green_esmerald), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = colorResource(id = R.color.black_2),
                                modifier = Modifier.size(20.dp).padding(start = 4.dp) // Centrar visualmente el icono de enviar
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No se encontraron detalles botánicos.",
                    color = colorResource(id = R.color.secondary_text)
                )
            }
        }
    }
}

// Componente auxiliar para las filas de información
@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorResource(id = R.color.violet_electric),
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.secondary_text)
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorResource(id = R.color.principal_text),
                    lineHeight = 22.sp // Mejor legibilidad para textos largos
                )
            )
        }
    }
}