package com.skydoves.moviecompose.ui.main

import android.os.Build
import android.webkit.WebSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skydoves.moviecompose.accounts.OdooManager
import com.skydoves.moviecompose.addons.WebAddonsRepository
import com.skydoves.moviecompose.models.entities.Database
import com.skydoves.moviecompose.models.entities.OdooAuthenticate
import com.skydoves.moviecompose.ui.components.CustomWebView
import timber.log.Timber

sealed class Screens(val route: String, val title: String) {

    sealed class HomeScreens(
        route: String,
        title: String,
        val icon: ImageVector
    ) : Screens(
        route,
        title
    ) {
        object Favorite : HomeScreens("favorite", "Favorite", Icons.Filled.Favorite)
        object Notification : HomeScreens("notification", "Notification", Icons.Filled.Notifications)
        object MyNetwork : HomeScreens("network", "MyNetwork", Icons.Filled.Person)
    }

    sealed class DrawerScreens(
        route: String,
        title: String
    ) : Screens(route, title) {
        object Home : DrawerScreens("home", "Home")
        object Account : DrawerScreens("account", "Account")
        object Help : DrawerScreens("help", "Help")
    }
}

val screensInHomeFromBottomNav = listOf(
    Screens.HomeScreens.Favorite,
    Screens.HomeScreens.Notification,
    Screens.HomeScreens.MyNetwork
)

val screensFromDrawer = listOf(
    Screens.DrawerScreens.Home,
    Screens.DrawerScreens.Account,
    Screens.DrawerScreens.Help,
)

@Composable
fun Home(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    viewModel.setCurrentScreen(Screens.DrawerScreens.Home)
    val account: OdooAuthenticate? by viewModel.currentAccountFlow.collectAsState(initial = null)

    LaunchedEffect(key1 = "" ) {
        viewModel.loadCurrentAccount()
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
//            .width(300.dp).height(500.dp)
            .background(Color.Gray),

//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home.", style = MaterialTheme.typography.h4)

//        account?.cookie?.let {
            CustomWebView(
                modifier = Modifier.fillMaxSize(),
//                url = "${OdooManager.serverUrl!!}",
                url = "file:///android_asset/image_detail.html",
                cookie = "",
                viewModel = viewModel,
                onProgressChange = { progress ->
//                rememberWebViewProgress = progress
                    Timber.d("onProgressChange: $progress")
                },
                initSettings = { settings ->
                    settings?.apply {
                        //支持js交互
                        javaScriptEnabled = true
                        //将图片调整到适合webView的大小
                        useWideViewPort = true
                        //缩放至屏幕的大小
                        loadWithOverviewMode = true
                        //缩放操作
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        //是否支持通过JS打开新窗口
                        javaScriptCanOpenWindowsAutomatically = true
                        //不加载缓存内容
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                },
                onBack = { webView ->
                    if (webView?.canGoBack() == true) {
                        webView.goBack()
                    } else {
//                    finish()
                    }
                },
                onReceivedError = { error ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Timber.d("onReceivedError: ${error?.description}")
                    }
                }
            )
//        }
    }
}


@Composable
fun Favorite(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    viewModel.setCurrentScreen(Screens.HomeScreens.Favorite)
//    val clickCount by viewModel.clickCount.observeAsState()
//    var click = clickCount?: 0
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Favorite.", style = MaterialTheme.typography.h4)
        Button(
            onClick = {
//                click++
//                viewModel.updateClick(click)
            }
        ) {
//            Text("clicked: $click")
        }
    }
}

@Composable
fun Notification(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    viewModel.setCurrentScreen(Screens.HomeScreens.Notification)
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Notification.", style = MaterialTheme.typography.h4)
    }
}

@Composable
fun MyNetwork(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    viewModel.setCurrentScreen(Screens.HomeScreens.MyNetwork)
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "MyNetwork.", style = MaterialTheme.typography.h4)
    }
}

@Composable
fun Account(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    viewModel.setCurrentScreen(Screens.DrawerScreens.Account)
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Account.", style = MaterialTheme.typography.h4)
        Timber.d("Account. Coming......")
    }
}

@Composable
fun Help(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    viewModel.setCurrentScreen(Screens.DrawerScreens.Help)
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Help.", style = MaterialTheme.typography.h4)
    }
}
