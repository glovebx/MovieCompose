/*
 * Designed and developed by 2021 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.moviecompose.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.skydoves.landscapist.coil.LocalCoilImageLoader
import com.skydoves.moviecompose.ui.theme.MovieComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      CompositionLocalProvider(LocalCoilImageLoader provides viewModel.imageLoader, LocalBackPressedDispatcher provides this.onBackPressedDispatcher) {
        MovieComposeTheme {
//                MainScreen()
                AppScaffold()
        }
      }
    }
  }
}


@Composable
fun AppScaffold() {
    val viewModel: MainViewModel = hiltViewModel()
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val currentScreen = viewModel.currentScreen

    if (scaffoldState.drawerState.isOpen) {
        BackPressHandler {
            scope.launch {
                scaffoldState.drawerState.close()
            }
        }
    }

    var topBar: @Composable () -> Unit = {}
    if (viewModel.clickCount.value == 1) {
        topBar = {
            TopBar(
                title = currentScreen!!.value.title,
                buttonIcon = Icons.Filled.Menu,
                onButtonClicked = {
                    scope.launch {
                        scaffoldState.drawerState.open()
                    }
                }
            )
        }

        if (currentScreen.value == Screens.DrawerScreens.Help) {
            topBar = {
                TopBar(
                    title = Screens.DrawerScreens.Help.title,
                    buttonIcon = Icons.Filled.ArrowBack,
                    onButtonClicked = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

//
//    val bottomBar: @Composable () -> Unit = {
//        if (currentScreen.value == Screens.DrawerScreens.Home
//            || currentScreen.value is Screens.HomeScreens) {
//            BottomBar(
//                navController = navController,
//                screens = screensInHomeFromBottomNav
//            )
//        }
//    }

//    ProvideWindowInsets {
//    }
    Scaffold(
        topBar = {
            topBar()
        },
//        bottomBar = {
//            bottomBar()
//        },
        scaffoldState = scaffoldState,
        drawerContent = {
            Drawer { route ->
                scope.launch {
                    scaffoldState.drawerState.close()
                }
                navController.navigate(route) {
                    popUpTo(route)
                    launchSingleTop = true
                }
            }
        },
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
    ) {
        if (viewModel.clickCount.value == 0) NavHost(navController = navController, startDestination = "login_screen",
            builder = {
            composable("login_screen", content = { LoginScreen(navController = navController, viewModel = viewModel) })
            composable(
                "register_screen",
                content = { RegistrationScreen(navController = navController) })
        }) else NavigationHost(navController = navController, viewModel = viewModel)
    }
}

@Composable
fun NavigationHost(navController: NavController, viewModel: MainViewModel) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screens.DrawerScreens.Home.route
    ) {
        composable(Screens.DrawerScreens.Home.route) {
            Home(viewModel = viewModel)
        }
        composable(Screens.HomeScreens.Favorite.route) { Favorite(viewModel = viewModel) }
        composable(Screens.HomeScreens.Notification.route) { Notification(viewModel = viewModel) }
        composable(Screens.HomeScreens.MyNetwork.route) { MyNetwork(viewModel = viewModel) }
        composable(Screens.DrawerScreens.Account.route) { Account(viewModel = viewModel) }
        composable(Screens.DrawerScreens.Help.route) { Help(viewModel = viewModel) }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MovieComposeTheme {
        AppScaffold()
    }
}

