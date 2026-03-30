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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
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

    Scaffold(
        containerColor = colorResource(id = R.color.black_2),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro de Flora",
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.green_esmerald)
                    )
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
        // VISTAS JETPACK COMPOSE REDISEÑADAS
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {

            item {
                Text(
                    text = "Información Botánica",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colorResource(id = R.color.violet_electric),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OrganicInput(
                    value = nombreEspecie,
                    onValueChange = { nombreEspecie = it },
                    label = "Nombre de la especie",
                    icon = Icons.Default.EnergySavingsLeaf
                )
            }

            item {
                OrganicInput(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = "Descripción",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    lines = 4
                )
            }

            item {
                OrganicInput(
                    value = cuidados,
                    onValueChange = { cuidados = it },
                    label = "Cuidados",
                    icon = Icons.Default.MedicalServices,
                    singleLine = false,
                    lines = 2
                )
            }

            item {
                HorizontalDivider(color = colorResource(id = R.color.gray_antracita), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colorResource(id = R.color.violet_electric),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Alcaldía", color = colorResource(id = R.color.secondary_text)) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = colorResource(id = R.color.secondary_text)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.green_esmerald),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = colorResource(id = R.color.gray_antracita),
                            unfocusedContainerColor = colorResource(id = R.color.gray_antracita),
                            focusedTextColor = colorResource(id = R.color.principal_text),
                            unfocusedTextColor = colorResource(id = R.color.principal_text)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier.background(colorResource(id = R.color.gray_antracita))
                    ) {
                        list.forEachIndexed { index, text ->
                            DropdownMenuItem(
                                text = { Text(text = text, color = colorResource(id = R.color.principal_text)) },
                                onClick = {
                                    selectedText = list[index]
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.gray_antracita)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Coordenadas", color = colorResource(id = R.color.secondary_text), fontSize = 12.sp)
                            Text(
                                text = if (localizacion.isEmpty()) "Pendiente..." else localizacion,
                                color = if (localizacion.isEmpty()) colorResource(id = R.color.secondary_text) else colorResource(id = R.color.principal_text),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                // TU LÓGICA EXACTA DE UBICACIÓN
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
                            modifier = Modifier
                                .background(colorResource(id = R.color.violet_electric).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Obtener ubicación", tint = colorResource(id = R.color.violet_electric))
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = colorResource(id = R.color.gray_antracita), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                // BOTÓN: SUBIR IMAGEN (Tu lógica exacta)
                OutlinedButton(
                    onClick = {
                        // Intent para abrir la carpeta de Google Drive
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/$driveFolderId"))
                        navController.context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(id = R.color.principal_text)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(id = R.color.secondary_text))
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subir Imagen")
                }
            }

            item {
                // BOTÓN: ENVIAR DATOS (Tu lógica exacta de validación y Firebase)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.green_esmerald),
                        contentColor = colorResource(id = R.color.black_2)
                    )
                ) {
                    Icon(Icons.Default.Spa, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar datos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// COMPONENTE UI PARA LOS INPUTS (Conserva el diseño moderno)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganicInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true,
    lines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = colorResource(id = R.color.secondary_text)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = colorResource(id = R.color.secondary_text)) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (singleLine) 56.dp else (lines * 40).dp),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else lines,
        shape = RoundedCornerShape(24.dp),
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
}

// TUS FUNCIONES EXACTAS AL FINAL DEL ARCHIVO
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
                        Log.d("DriveAPI", "URL formateada: $formattedUrl") // Verifica la URL generada
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