package io.devexpert.appfloracdmx

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authManager: AuthManager,
    navController: NavHostController,
    onSuccessLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun signIn() {
        scope.launch {
            val user = authManager.signIn(email, password)
            if (user != null) {
                onSuccessLogin()
                navController.navigate("pantalla_menuFlora")
            } else {
                errorMessage = "Error al iniciar sesión, por favor valide su correo y contraseña."
                showDialog = true
            }
        }
    }

    fun signUp() {
        scope.launch {
            try {
                val user = authManager.signUp(email, password, nickname)
                if (user != null) {
                    onSuccessLogin()
                    navController.navigate("pantalla_menuFlora")
                } else {
                    errorMessage = "Error al registrar su cuenta, verifique su correo o apodo."
                    showDialog = true
                }
            } catch (e: Exception) {
                if (e.message?.contains("nickname already exists") == true) {
                    errorMessage = "El apodo ya está en uso. Por favor, elija otro."
                } else {
                    errorMessage = e.message ?: "Error desconocido al registrarse."
                }
                showDialog = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.black_2))
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colorResource(id = R.color.black_2).copy(alpha = 0.9f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.gray_antracita).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_login),
                        contentDescription = "Logo Flora",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSignUpMode) "Crear Cuenta" else "Bienvenido",
                        color = colorResource(id = R.color.principal_text),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Registra y monitorea tus plantas",
                        color = colorResource(id = R.color.secondary_text),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OrganicTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        icon = Icons.Filled.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OrganicTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        icon = Icons.Filled.Lock,
                        isPassword = true
                    )

                    AnimatedVisibility(
                        visible = isSignUpMode,
                        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OrganicTextField(
                                value = nickname,
                                onValueChange = { nickname = it },
                                label = "Apodo botánico",
                                icon = Icons.Filled.Person
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { if (isSignUpMode) signUp() else signIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.green_esmerald),
                            contentColor = colorResource(id = R.color.black_2)
                        )
                    ) {
                        Text(
                            text = if (isSignUpMode) "Registrarse" else "Iniciar sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                        Text(
                            text = if (isSignUpMode) "¿Ya tienes cuenta? Inicia sesión" else "¿Nuevo aquí? Crea una cuenta",
                            color = colorResource(id = R.color.violet_electric),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!isSignUpMode) {
                        TextButton(onClick = { navController.navigate("pantalla_recuperar") }) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                color = colorResource(id = R.color.secondary_text),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            MyDialog(
                title = if (isSignUpMode) "Error de Registro" else "Error de Autenticación",
                text = errorMessage ?: "Ocurrió un error inesperado.",
                onDismiss = { showDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = colorResource(id = R.color.secondary_text)) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = colorResource(id = R.color.secondary_text))
        },
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = null, tint = colorResource(id = R.color.secondary_text))
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email),
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(id = R.color.green_esmerald),
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = colorResource(id = R.color.black_2).copy(alpha = 0.5f),
            unfocusedContainerColor = colorResource(id = R.color.black_2).copy(alpha = 0.3f),
            focusedTextColor = colorResource(id = R.color.principal_text),
            unfocusedTextColor = colorResource(id = R.color.principal_text),
            cursorColor = colorResource(id = R.color.green_esmerald)
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun MyDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        containerColor = colorResource(id = R.color.gray_antracita),
        titleContentColor = colorResource(id = R.color.principal_text),
        textContentColor = colorResource(id = R.color.secondary_text),
        onDismissRequest = { onDismiss() },
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    text = "Entendido",
                    color = colorResource(id = R.color.green_esmerald),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}