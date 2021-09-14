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
import com.skydoves.moviecompose.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
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


    private val _versionSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)

    val versionFlow = _versionSharedFlow.flatMapLatest {
        authRepository.loadVersionInfo(it)
    }

    fun fetchServerVersionInfo(url: String) = _versionSharedFlow.tryEmit(url)
}

enum class Particle { SERVER_URL_INPUT, DATABASE_SELECT, SIGN_IN }
