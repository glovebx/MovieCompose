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
import com.skydoves.moviecompose.models.OdooLogin
import com.skydoves.moviecompose.models.network.TaskExecuteState
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

    private val _particle = mutableStateOf(Particle.AUTO_AUTHENTICATE)
    val particle: State<Particle> get() = _particle

    val switchParticle: (Particle) -> Unit = {
        _particle.value = it
    }

    // 是否登录成功
    private val _odooAuthenticated = mutableStateOf(false)
    val odooAuthenticated: State<Boolean> get() = _odooAuthenticated
    val onOdooAuthenticated: (Boolean) -> Unit = {
        _odooAuthenticated.value = it
    }

    private val _taskExecuteState: MutableState<TaskExecuteState> = mutableStateOf(TaskExecuteState.IDLE)
    val taskExecuteState: State<TaskExecuteState> get() = _taskExecuteState

    private val _versionAndDatabaseFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    val versionAndDatabaseFlow = _versionAndDatabaseFlow.flatMapLatest {
        if (it.isNullOrEmpty()) {
            flow {
                emit(null)
            }
        } else {
            _taskExecuteState.value = TaskExecuteState.LOADING
            authRepository.loadVersionAndDatabaseInfo(url = it,
                success = { _taskExecuteState.value = TaskExecuteState.SUCCESS },
                error = { code, message -> _taskExecuteState.value = TaskExecuteState.ERROR(code, message) }
            )
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
    fun fetchServerBasicInfo(url: String) = _versionAndDatabaseFlow.tryEmit(url)
    fun clearServerBasicInfo() = _versionAndDatabaseFlow.tryEmit("")

    private val _databaseNameFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    val databaseNameFlow = _databaseNameFlow.flatMapLatest {
        if (it.isNullOrEmpty()) {
            flow {
                emit(null)
            }
        } else {
            authRepository.setupDatabaseName(it)
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
    fun setupDatabaseName(db: String) = _databaseNameFlow.tryEmit(db)
    fun clearDatabaseName() = _databaseNameFlow.tryEmit("")

    // 用户名口令验证
    private val _authenticateFlow: MutableSharedFlow<OdooLogin> = MutableSharedFlow(replay = 1)
    val authenticateFlow = _authenticateFlow.flatMapLatest {
        if (it.login.isNullOrEmpty()) {
            flow {
                emit(null)
            }
        } else {
            _taskExecuteState.value = TaskExecuteState.LOADING
            authRepository.authenticate(odooLogin = it,
                success = { _taskExecuteState.value = TaskExecuteState.SUCCESS }
            )
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
    fun authenticate(odooLogin: OdooLogin) = _authenticateFlow.tryEmit(odooLogin)

    // 验证既存的有效账号
    private val _authenticateCurrentFlow: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1)
    val authenticateCurrentFlow = _authenticateCurrentFlow.flatMapLatest {
        if (it == 0) {
            flow {
                emit(null)
            }
        } else {
            _taskExecuteState.value = TaskExecuteState.LOADING
            authRepository.authenticate(
                success = { _taskExecuteState.value = TaskExecuteState.SUCCESS }
            )
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
    fun startCurrentAuthenticate(active: Int = 1) = _authenticateCurrentFlow.tryEmit(active)

    fun clearAuthenticate() {
        _authenticateFlow.tryEmit(OdooLogin())
        _authenticateCurrentFlow.tryEmit(0)
    }

}

enum class Particle { AUTO_AUTHENTICATE, SERVER_URL_INPUT, DATABASE_INPUT, DATABASE_SELECT, SIGN_IN }

enum class AuthenticateResult { SESSION_EXPIRED, SWITCH_ACCOUNT, ACCOUNT_NOT_EXISTS, AUTHENTICATE_FAILED, AUTHENTICATED }
