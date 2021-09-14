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

package com.skydoves.moviecompose.ui.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.moviecompose.accounts.OdooManager
import com.skydoves.moviecompose.models.network.NetworkState
import com.skydoves.moviecompose.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    init {
        Timber.d("Injection AuthViewModel")
    }

    private val _particle = mutableStateOf(Particle.SERVER_URL_INPUT)
    val particle: State<Particle> get() = _particle

    val switchParticle: (Particle) -> Unit = {
        _particle.value = it
    }


    private val _authLoadingState: MutableState<NetworkState> = mutableStateOf(NetworkState.IDLE)
    val authLoadingState: State<NetworkState> get() = _authLoadingState

    private val _versionAndDatabaseSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    val versionAndDatabaseFlow = _versionAndDatabaseSharedFlow.flatMapLatest {
        if (it == "NULL") {
            flow {
                emit(null)
            }
        } else {
            _authLoadingState.value = NetworkState.LOADING
            authRepository.loadVersionAndDatabaseInfo(url = it,
                success = { _authLoadingState.value = NetworkState.SUCCESS },
                error = { _authLoadingState.value = NetworkState.ERROR }
            )
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
    fun fetchServerBasicInfo(url: String) = _versionAndDatabaseSharedFlow.tryEmit(url)
    fun clearServerBasicInfo() = _versionAndDatabaseSharedFlow.tryEmit("NULL")

    private val _databaseNameSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    val databaseNameFlow = _databaseNameSharedFlow.flatMapLatest {
        authRepository.setupDatabaseName(it)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
    fun setupDatabaseName(db: String) = _databaseNameSharedFlow.tryEmit(db)


    private val _authenticateSharedFlow: MutableSharedFlow<Map<String, String>> = MutableSharedFlow(replay = 1)
    val authenticateFlow = _authenticateSharedFlow.flatMapLatest {
        _authLoadingState.value = NetworkState.LOADING
        authRepository.authenticate(db = it["db"]!!, login = it["login"]!!, password = it["password"]!!,
            success = { _authLoadingState.value = NetworkState.SUCCESS },
            error = { _authLoadingState.value = NetworkState.ERROR }
        )
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
    fun authenticate(db: String, login: String, password: String) = _authenticateSharedFlow.tryEmit(mapOf("db" to db, "login" to login, "password" to password))
}

enum class Particle { SERVER_URL_INPUT, DATABASE_INPUT, DATABASE_SELECT, SIGN_IN }
