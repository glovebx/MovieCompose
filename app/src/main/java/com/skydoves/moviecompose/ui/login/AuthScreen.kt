package com.skydoves.moviecompose.ui.login

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.skydoves.moviecompose.accounts.OdooManager
import com.skydoves.moviecompose.models.entities.Database
import com.skydoves.moviecompose.models.network.JsonRpcCallState
import com.skydoves.moviecompose.models.network.NetworkState
import com.skydoves.moviecompose.models.network.onError
import com.skydoves.moviecompose.models.network.onLoading
import com.skydoves.moviecompose.ui.theme.MovieComposeTheme
import kotlinx.coroutines.launch


@Composable
fun AuthScreen(navController: NavController, viewModel: AuthViewModel) {
    val particle by viewModel.particle

    MovieComposeTheme() {
        Crossfade(targetState = particle) {
            when (it) {
                Particle.AUTO_AUTHENTICATE -> {
                    AutoAuthenticateScreen(viewModel = viewModel)
                }
                Particle.SERVER_URL_INPUT -> {
                    ServerUrlScreen(viewModel = viewModel)
                }
                Particle.DATABASE_INPUT -> {
                    DatabaseInputScreen(viewModel = viewModel)
                }
                Particle.DATABASE_SELECT -> {
                    RegistrationScreen(navController = navController, viewModel = viewModel)
                }
                Particle.SIGN_IN -> {
                    LoginScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AutoAuthenticateScreen(viewModel: AuthViewModel) {
    val networkState: NetworkState by viewModel.authLoadingState
    val authenticateResult: AuthenticateResult? by viewModel.authenticateCurrentFlow.collectAsState(initial = null)

    LaunchedEffect(key1 = "") {
        viewModel.startCurrentAuthenticate()
    }

    when(authenticateResult) {
        AuthenticateResult.SESSION_EXPIRED -> {
            // session过期，直接到登录界面
            viewModel.switchParticle(Particle.SIGN_IN)
        }
        AuthenticateResult.AUTHENTICATE_FAILED -> {
            // 账号有效，直接进入业务界面
            viewModel.switchParticle(Particle.SIGN_IN)
        }
        AuthenticateResult.SWITCH_ACCOUNT -> {
            // 切换账号，到登录界面
            viewModel.switchParticle(Particle.SIGN_IN)
        }
        AuthenticateResult.ACCOUNT_NOT_EXISTS -> {
            // 没有账号，到serverUrl输入界面
            viewModel.switchParticle(Particle.SERVER_URL_INPUT)
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
                append("A")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("uthenticating")
            }
        }, fontSize = 30.sp)
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

@Composable
fun ServerUrlScreen(viewModel: AuthViewModel) {
//    val context = LocalContext.current
    val networkState: NetworkState by viewModel.authLoadingState
    val databaseList: List<Database>? by viewModel.versionAndDatabaseFlow.collectAsState(initial = null)

    val jsonRpcCallState: JsonRpcCallState by viewModel.jsonRpcCallState

    if (databaseList != null) {
        when (databaseList?.size) {
            0 -> {
                // 输入数据库名字
                if (viewModel.particle.value != Particle.DATABASE_INPUT) {
                    viewModel.switchParticle(Particle.DATABASE_INPUT)
                }
            }
            1 -> {
                OdooManager.db = databaseList!![0].db
                if (viewModel.particle.value != Particle.SIGN_IN) {
                    viewModel.switchParticle(Particle.SIGN_IN)
                }
            }
            else -> {
                if (viewModel.particle.value != Particle.DATABASE_SELECT) {
                    viewModel.switchParticle(Particle.DATABASE_SELECT)
                }
            }
        }
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
            onValueChange = {
                if (serverUrlErrorState.value) {
                    serverUrlErrorState.value = false
                }
                serverUrl.value = it
            },
            isError = serverUrlErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Enter Server Url*")
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
//                        serverUrlErrorState.value = false
//                        Toast.makeText(
//                            context,
//                            "Connect in successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        viewModel.fetchServerBasicInfo(serverUrl.value.text)
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
//
//    networkState.onError {
//        serverUrlErrorState.value = true
//    }

    jsonRpcCallState.onError {
        code, message -> if (code == 1) {

    } else {

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

@Composable
fun DatabaseInputScreen(viewModel: AuthViewModel) {
    val db: String? by viewModel.databaseNameFlow.collectAsState(initial = null)

    db?.apply {
        if (viewModel.particle.value != Particle.SIGN_IN) {
            viewModel.switchParticle(Particle.SIGN_IN)
        }
    }

    val databaseName = remember { mutableStateOf(TextFieldValue()) }
    val databaseNameErrorState = remember { mutableStateOf(false) }
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
                append(" D")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("atabase")
            }

            withStyle(style = SpanStyle(color = Color.Red)) {
                append(" N")
            }
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("ame")
            }
        }, fontSize = 30.sp)
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = databaseName.value,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
            onValueChange = {
                if (databaseNameErrorState.value) {
                    databaseNameErrorState.value = false
                }
                databaseName.value = it
            },
            isError = databaseNameErrorState.value,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Enter Database Name*")
            },
        )
        if (databaseNameErrorState.value) {
            Text(text = "Required", color = Color.Red)
        }
        Spacer(Modifier.size(16.dp))
        Button(
            onClick = {
                when {
                    databaseName.value.text.isEmpty() -> {
                        databaseNameErrorState.value = true
                    }
                    else -> {
//                        serverUrlErrorState.value = false
//                        Toast.makeText(
//                            context,
//                            "Connect in successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        viewModel.setupDatabaseName(databaseName.value.text)
                    }
                }

            },
            content = {
                Text(text = "Confirm", color = Color.White)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
        )
        Spacer(Modifier.size(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = {
                viewModel.clearServerBasicInfo()
                viewModel.switchParticle(Particle.SERVER_URL_INPUT)
            }) {
                Text(text = "< Back", color = Color.Red)
            }
        }
    }
}
