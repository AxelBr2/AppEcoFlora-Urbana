package io.devexpert.appfloracdmx

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloraRegistrationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = Firebase.storage

    // ESTADOS DEL FORMULARIO
    var nombreEspecie by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var cuidados by remember { mutableStateOf("") }
    var localizacion by remember { mutableStateOf("") }
    var selectedText by remember { mutableStateOf("Álvaro Obregón") }
    var isExpanded by remember { mutableStateOf(false) }

    // ESTADOS DE IMAGEN HD
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var isSubiendo by remember { mutableStateOf(false) }

    val list = listOf(
        "Álvaro Obregón", "Azcapotzalco", "Benito Juárez", "Coyoacán", "Cuajimalpa",
        "Cuauhtémoc", "Gustavo A. Madero", "Iztacalco", "Iztapalapa", "Magdalena Contreras",
        "Miguel Hidalgo", "Milpa Alta", "Tláhuac", "Tlalpan", "Venustiano Carranza", "Xochimilco"
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Función para crear la ruta del archivo temporal (HD)
    fun crearImagenUri(): Uri {
        val directory = File(context.externalCacheDir, "flora_photos")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "captura_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // LAUNCHER CÁMARA HD
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) fotoUri = null
    }

    // LAUNCHER PERMISOS
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

        if (locationGranted) {
            fetchLocation(fusedLocationClient) { coords -> localizacion = coords }
        }
        if (cameraGranted) {
            val uri = crearImagenUri()
            fotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        containerColor = colorResource(id = R.color.black_2),
        topBar = {
            TopAppBar(
                title = { Text("Registro de Flora HD", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.green_esmerald)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.black_2))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text("Información Botánica", style = MaterialTheme.typography.titleMedium.copy(), color = colorResource(id = R.color.violet_electric), fontWeight = FontWeight.Bold)

                OrganicInput(
                    value = nombreEspecie,
                    onValueChange = {nombreEspecie = it},
                    label = "Nombre de la especie",
                    icon = Icons.Default.EnergySavingsLeaf
                )
            }

            item {
                OrganicInput(
                    value = descripcion,
                    onValueChange = {descripcion = it},
                    label = "Descripción",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    lines = 4
                )
            }

            item {
                OrganicInput(
                    value = cuidados,
                    onValueChange = {cuidados = it},
                    label = "Cuidados",
                    icon = Icons.Default.MedicalServices,
                    singleLine = false,
                    lines = 2
                )
            }

            item {
                HorizontalDivider(color = colorResource(id = R.color.gray_antracita), thickness = 1.dp)
                Text(text = "Ubicación", style = MaterialTheme.typography.titleMedium.copy(color = colorResource(id = R.color.violet_electric), fontWeight = FontWeight.Bold))

                ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = !isExpanded }) {
                    OutlinedTextField(
                        value = selectedText, onValueChange = {}, readOnly = true,
                        label = { Text("Alcaldía", color = colorResource(id = R.color.secondary_text)) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = colorResource(id = R.color.secondary_text)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.green_esmerald),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = colorResource(id = R.color.gray_antracita),
                            unfocusedContainerColor = colorResource(id = R.color.gray_antracita),
                            focusedTextColor = colorResource(id = R.color.secondary_text),
                            unfocusedTextColor = colorResource(id = R.color.secondary_text)
                        )
                    )
                    ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                        list.forEach { text ->
                            DropdownMenuItem(text = { Text(text) }, onClick = { selectedText = text; isExpanded = false })
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.gray_antracita)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Coordenadas", color = colorResource(id = R.color.secondary_text), fontSize = 12.sp)
                            Text(text = localizacion.ifEmpty { "Pendiente..." }, color = colorResource(id = R.color.secondary_text), fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) },
                            modifier = Modifier.background(colorResource(id = R.color.violet_electric).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) { Icon(Icons.Default.Map, null, tint = colorResource(id = R.color.violet_electric)) }
                    }
                }
            }

            item {
                HorizontalDivider(color = colorResource(id = R.color.gray_antracita), thickness = 1.dp)

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colorResource(id = R.color.secondary_text)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (fotoUri == null) "Tomar Foto HD" else "Cambiar Imagen")
                    }

                    fotoUri?.let { uri ->
                        Spacer(Modifier.height(12.dp))
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(180.dp).clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (nombreEspecie.isNotBlank() && fotoUri != null && localizacion.isNotBlank()) {
                            isSubiendo = true
                            subirInfoFirebase(
                                uri = fotoUri!!,
                                storage = storage,
                                db = db,
                                data = mapOf(
                                    "nombreEspecie" to nombreEspecie,
                                    "descripcion" to descripcion,
                                    "cuidados" to cuidados,
                                    "alcaldia" to selectedText,
                                    "localizacion" to localizacion
                                )
                            ) { success ->
                                isSubiendo = false
                                if (success) {
                                    Toast.makeText(context, "¡Planta Registrada con Éxito!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Datos incompletos o falta imagen", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(50),
                    enabled = !isSubiendo,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green_esmerald))
                ) {
                    if (isSubiendo) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    else {
                        Icon(Icons.Default.Spa, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar datos", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- COMPONENTE DE UI PARA INPUTS ---
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
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

// --- FUNCIONES DE APOYO ---
@SuppressLint("MissingPermission")
fun fetchLocation(fusedLocationClient: FusedLocationProviderClient, onLocationFetched: (String) -> Unit) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) onLocationFetched("${location.latitude}, ${location.longitude}")
        else onLocationFetched("Ubicación no disponible")
    }
}

fun subirInfoFirebase(
    uri: Uri,
    storage: com.google.firebase.storage.FirebaseStorage,
    db: com.google.firebase.firestore.FirebaseFirestore,
    data: Map<String, String>,
    onResult: (Boolean) -> Unit
) {
    val storageRef = storage.reference.child("flora_fotos/HD_${System.currentTimeMillis()}.jpg")
    storageRef.putFile(uri).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            val finalData = data.toMutableMap()
            finalData["url"] = downloadUri.toString()
            db.collection("Flora_datos").add(finalData)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }
    }.addOnFailureListener { onResult(false) }
}