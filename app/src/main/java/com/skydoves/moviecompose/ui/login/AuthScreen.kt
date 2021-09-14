package com.skydoves.moviecompose.ui.login

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.skydoves.moviecompose.models.network.VersionResult
import com.skydoves.moviecompose.ui.theme.MovieComposeTheme


@Composable
fun AuthScreen(navController: NavController, viewModel: AuthViewModel) {
    val particle by viewModel.particle

    MovieComposeTheme() {
        Crossfade(particle) {
            when (it) {
                Particle.SERVER_URL_INPUT -> {
                    ServerUrlScreen(navController = navController, viewModel = viewModel)
                }
                Particle.SIGN_IN -> {
                    LoginScreen(navController = navController, viewModel = viewModel)
                }
                Particle.DATABASE_SELECT -> {
                    RegistrationScreen(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ServerUrlScreen(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current

    val versionResult: VersionResult? by viewModel.versionFlow.collectAsState(initial = null)

    versionResult?.data?.apply {
        viewModel.switchParticle(Particle.DATABASE_SELECT)
    }

    val serverUrl = remember { mutableStateOf(TextFieldValue()) }
    val serverUrlErrorState = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("I")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("put")
            }

            withStyle(style = SpanStyle(color = Color.Red)) {
                append(" S")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("erver")
            }

            withStyle(style = SpanStyle(color = Color.Red)) {
                append(" U")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("rl")
            }
        }, fontSize = 30.sp)
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = serverUrl.value,
            onValueChange = {
                if (serverUrlErrorState.value) {
                    serverUrlErrorState.value = false
                }
                serverUrl.value = it
            },
            isError = serverUrlErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Enter Email*")
            },
        )
        if (serverUrlErrorState.value) {
            Text(text = "Required", color = Color.Red)
        }
        Spacer(Modifier.size(16.dp))
        Button(
            onClick = {
                when {
                    serverUrl.value.text.isEmpty() -> {
                        serverUrlErrorState.value = true
                    }
                    else -> {
                        serverUrlErrorState.value = false
                        Toast.makeText(
                            context,
                            "Connect in successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.fetchServerVersionInfo(serverUrl.value.text)
                    }
                }

            },
            content = {
                Text(text = "Connect", color = Color.White)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
        )
    }
}
