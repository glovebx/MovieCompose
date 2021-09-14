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

package com.skydoves.moviecompose.repository

import androidx.annotation.WorkerThread
import com.skydoves.moviecompose.network.service.AuthService
import com.skydoves.moviecompose.persistence.MovieDao
import com.skydoves.sandwich.suspendOnSuccess
import com.skydoves.sandwich.toSuspendFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class AuthRepository constructor(
  private val authService: AuthService
) : Repository {

  init {
    Timber.d("Injection AuthRepository")
  }

  @WorkerThread
  fun loadVersionInfo(url: String) = flow {
    val params = mapOf<String, Any>()
    val response = authService.fetchVersionInfo(params)
    response.suspendOnSuccess {
        emit(data)
    }
  }
//      .flatMapLatest { flow {
//          val params = mapOf<String, Any>()
//          val response = authService.fetchDatabaseList(params)
//          response.suspendOnSuccess {
//              emit(data)
//          }
//      }
//  }
      .flowOn(Dispatchers.IO)
}
