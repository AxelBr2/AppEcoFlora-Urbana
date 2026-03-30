package io.devexpert.appfloracdmx

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloraRegistrationScreen(navController: NavHostController) {
    // INSTANCIA DE FIRESTORE
    val db = FirebaseFirestore.getInstance()

    // Define variables de estado para cada campo de texto
    var descripcion by remember { mutableStateOf("") }
    var nombreEspecie by remember { mutableStateOf("") }
    var cuidados by remember { mutableStateOf("") }
    var localizacion by remember { mutableStateOf("") }

    val list = listOf(
        "Álvaro Obregón", "Azcapotzalco", "Benito Juárez", "Coyoacán", "Cuajimalpa",
        "Cuauhtémoc", "Gustavo A. Madero", "Iztacalco", "Iztapalapa", "Magdalena Contreras",
        "Miguel Hidalgo", "Milpa Alta", "Tláhuac", "Tlalpan", "Venustiano Carranza", "Xochimilco"
    )
    var isExpanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(list[0]) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(navController.context)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation(fusedLocationClient) { location ->
                localizacion = location
            }
        } else {
            localizacion = "Permiso denegado"
        }
    }

    val driveFolderId = "133PDjgu39iS5jorBZmtZOojX92pOycoR"


    // VISTAS JETPACK COMPOSE
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 45.dp)
    ) {
        item {
            TopAppBar(
                title = { Text(text = "Registro de Flora") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE), // Color de fondo
                    titleContentColor = Color.White // Color del texto
                ),
                modifier = Modifier.padding(0.dp) // Sin padding extra
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Text(
                text = "Nombre de la especie:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nombreEspecie,
                onValueChange = { nombreEspecie = it },
                label = { Text("Nombre de la especie") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }

        item {
            Text(
                text = "Descripción:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(100.dp),
                maxLines = 6,
                singleLine = false
            )
        }

        item {
            Text(
                text = "Cuidados:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cuidados,
                onValueChange = { cuidados = it },
                label = { Text("Cuidados") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                maxLines = 2,
                singleLine = false
            )
        }

        item {
            Text(
                text = "Alcaldía:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = !isExpanded }
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor()
                        .padding(10.dp)
                        .fillMaxWidth(),
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier
                        .width(200.dp)
                        .heightIn(max = 150.dp)
                ) {
                    list.forEachIndexed { index, text ->
                        DropdownMenuItem(
                            text = { Text(text = text) },
                            onClick = {
                                selectedText = list[index]
                                isExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Ubicación:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localizacion,
                onValueChange = {},
                label = { Text("Coordenadas") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                readOnly = true
            )

            Button(
                onClick = {
                    if (ActivityCompat.checkSelfPermission(
                            navController.context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        fetchLocation(fusedLocationClient) { location ->
                            localizacion = location
                        }
                    }
                },
            ) {
                Text("Obtener ubicación")
            }
        }

        item {
            Button(
                onClick = {
                    // Intent para abrir la carpeta de Google Drive
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/$driveFolderId"))
                    navController.context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subir Imagen")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nombreEspecie.isNotBlank() && descripcion.isNotBlank() && cuidados.isNotBlank() && localizacion.isNotBlank() && localizacion.isNotBlank()) {
                        // Llama a la función para buscar la imagen más reciente
                        val context = navController.context
                        getMostRecentImageUrl(context, driveFolderId) { imageUrl ->
                            if (imageUrl != null) {
                                val formattedUrl = "https://drive.google.com/uc?id=$imageUrl"
                                val floraData = mapOf(
                                    "nombreEspecie" to nombreEspecie,
                                    "descripcion" to descripcion,
                                    "cuidados" to cuidados,
                                    "localizacion" to localizacion,
                                    "alcaldia" to selectedText,
                                    "url" to formattedUrl
                                )
                                db.collection("Flora_datos")
                                    .add(floraData)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "¡Datos enviados correctamente!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "No se encontró ninguna imagen", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(navController.context, "Campos incompletos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar datos")
            }

        }
    }
}
@SuppressLint("MissingPermission")
fun fetchLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (String) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val coordinates = "${location.latitude}, ${location.longitude}"
            onLocationFetched(coordinates)
        } else {
            onLocationFetched("No se pudo obtener la ubicación")
        }
    }
}

fun getMostRecentImageUrl(context: Context, folderId: String, callback: (String?) -> Unit) {
    try {
        // Carga las credenciales desde el archivo JSON
        val credentialsStream = context.resources.openRawResource(R.raw.credentials)
        val googleCredentials = GoogleCredentials.fromStream(credentialsStream)
            .createScoped(listOf(DriveScopes.DRIVE_READONLY))

        // Construir el cliente de Google Drive
        val httpRequestInitializer = HttpCredentialsAdapter(googleCredentials)
        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            httpRequestInitializer
        ).setApplicationName("FloraApp").build()

        // Usar corrutinas para realizar la solicitud
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Consulta los archivos en la carpeta específica y ordena por la fecha de modificación
                val result = driveService.files().list()
                    .setQ("'$folderId' in parents and mimeType contains 'image/'")
                    .setFields("files(id, name, modifiedTime)")
                    .setOrderBy("modifiedTime desc")
                    .execute()

                // Verifica si se obtuvo al menos un archivo
                val mostRecentFile = result.files.firstOrNull()

                // Si se encuentra un archivo, devuelve su URL
                withContext(Dispatchers.Main) {
                    if (mostRecentFile != null) {
                        // Log para verificar el ID del archivo recuperado
                        Log.d("DriveAPI", "Imagen encontrada: ${mostRecentFile.name}, ID: ${mostRecentFile.id}")

                        // Formato de la URL según lo solicitado
                        val formattedUrl = mostRecentFile.id
                        Log.d("DriveAPI", "URL formateada: $formattedUrl")  // Verifica la URL generada
                        callback(formattedUrl)
                    } else {
                        // Si no se encuentra ningún archivo, se pasa null
                        Log.d("DriveAPI", "No se encontró ninguna imagen.")
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("DriveAPI", "Error al listar archivos: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("DriveAPI", "Error al inicializar GoogleCredentials: ${e.message}")
        callback(null)
    }
}







