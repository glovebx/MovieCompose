package com.skydoves.moviecompose.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.moviecompose.accounts.OdooManager
import com.skydoves.moviecompose.models.OdooLogin
import com.skydoves.moviecompose.models.entities.OdooAuthenticate
import com.skydoves.moviecompose.models.network.NetworkState
import com.skydoves.moviecompose.models.network.onError
import com.skydoves.moviecompose.models.network.onLoading

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
//    val context = LocalContext.current
    val networkState: NetworkState by viewModel.authLoadingState
    val authenticateResult: AuthenticateResult? by viewModel.authenticateFlow.collectAsState(initial = null)

    val email = remember { mutableStateOf(TextFieldValue()) }
    val emailErrorState = remember { mutableStateOf(false) }
    val passwordErrorState = remember { mutableStateOf(false) }
    val password = remember { mutableStateOf(TextFieldValue()) }

    val loginErrorState = remember { mutableStateOf(false) }

    when(authenticateResult) {
        AuthenticateResult.AUTHENTICATE_FAILED -> {
            // 登录错误
            loginErrorState.value = true
        }
        AuthenticateResult.AUTHENTICATED -> {
            // 账号有效，直接进入业务界面
            viewModel.onOdooAuthenticated(true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("S")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("ign")
            }

            withStyle(style = SpanStyle(color = Color.Red)) {
                append(" I")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("n")
            }
        }, fontSize = 30.sp)
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = email.value,
            onValueChange = {
                if (loginErrorState.value) {
                    loginErrorState.value = false
                }
                if (emailErrorState.value) {
                    emailErrorState.value = false
                }
                email.value = it
            },
            isError = emailErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Enter Email*")
            },
        )
        if (emailErrorState.value) {
            Text(text = "Required", color = Color.Red)
        }
        Spacer(Modifier.size(16.dp))
        val passwordVisibility = remember { mutableStateOf(true) }
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                if (loginErrorState.value) {
                    loginErrorState.value = false
                }
                if (passwordErrorState.value) {
                    passwordErrorState.value = false
                }
                password.value = it
            },
            isError = passwordErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Enter Password*")
            },
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility.value = !passwordVisibility.value
                }) {
                    Icon(
                        imageVector = if (passwordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "visibility",
                        tint = Color.Red
                    )
                }
            },
            visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
        )
        if (passwordErrorState.value) {
            Text(text = "Required", color = Color.Red)
        }
        if (loginErrorState.value) {
            Text(text = "Login Failed", color = Color.Red)
        }
        Spacer(Modifier.size(16.dp))
        Button(
            onClick = {
                when {
                    email.value.text.isEmpty() -> {
                        emailErrorState.value = true
                    }
                    password.value.text.isEmpty() -> {
                        passwordErrorState.value = true
                    }
                    else -> {
                        passwordErrorState.value = false
                        emailErrorState.value = false
                        loginErrorState.value = false
//                        Toast.makeText(
//                            context,
//                            "Logged in successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        navController.popBackStack()
//                        viewModel.switchParticle(Particle.DATABASE_SELECT)
//                        viewModel.fetchServerVersionInfo()
                        viewModel.authenticate(OdooLogin(OdooManager.serverUrl!!,
                            OdooManager.db!!, email.value.text, password.value.text))
                    }
                }

            },
            content = {
                Text(text = "Login", color = Color.White)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
        )
        Spacer(Modifier.size(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = {
//                navController.navigate("register_screen") {
//                    popUpTo(navController.graph.startDestinationId)
//                    launchSingleTop = true
//                }
                viewModel.clearAuthenticate()
                viewModel.clearDatabaseName()
                viewModel.switchParticle(Particle.DATABASE_INPUT)
            }) {
                Text(text = "< Back", color = Color.Red)
            }
        }
    }


    networkState.onLoading {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
