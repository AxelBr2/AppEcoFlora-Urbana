package io.devexpert.appfloracdmx

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

// Asegúrate de importar tu clase AuthManager y tu R (ej. import com.tu.paquete.R)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecuperar(navController: NavHostController) {
    // LÓGICA INTACTA
    val auth = FirebaseAuth.getInstance()
    val authManager = AuthManager(auth)

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    // UI MODERNA DE ECOFLORA
    Scaffold(
        containerColor = colorResource(id = R.color.black_2),
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
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
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Ícono decorativo
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = colorResource(id = R.color.green_esmerald),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cambiar contraseña",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colorResource(id = R.color.principal_text),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ingresa tu correo electrónico registrado y te enviaremos un enlace para restablecer tu acceso.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorResource(id = R.color.secondary_text)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo de texto estándar delineado e integrado directamente
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico", color = colorResource(id = R.color.secondary_text)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Ícono de correo",
                        tint = colorResource(id = R.color.secondary_text)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.green_esmerald),
                    unfocusedBorderColor = colorResource(id = R.color.secondary_text),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = colorResource(id = R.color.principal_text),
                    unfocusedTextColor = colorResource(id = R.color.principal_text),
                    cursorColor = colorResource(id = R.color.green_esmerald)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón principal
            Button(
                onClick = {
                    sendPasswordResetEmail(auth, email) { result ->
                        message = result
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
                Text("Restablecer contraseña", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Mensaje de estado
            message?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = it,
                    color = if (it.contains("enviado")) colorResource(id = R.color.green_esmerald) else Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// LÓGICA INTACTA
fun sendPasswordResetEmail(auth: FirebaseAuth, email: String, onResult: (String) -> Unit) {
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult("Se ha enviado una liga de restablecimiento al correo proporcionado.")
            } else {
                onResult("Error: ${task.exception?.localizedMessage ?: "No se pudo enviar el correo."}")
            }
        }
}