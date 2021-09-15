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

package com.skydoves.moviecompose.models.network

import androidx.compose.runtime.Composable

sealed class JsonRpcCallState {
    object IDLE : JsonRpcCallState()
    object LOADING : JsonRpcCallState()
    object SUCCESS : JsonRpcCallState()
    class ERROR(val code: Int, val message: String) : JsonRpcCallState()
}

@Composable
fun JsonRpcCallState.onSuccess(block: @Composable () -> Unit): JsonRpcCallState {
  if (this == JsonRpcCallState.SUCCESS) {
    block()
  }
  return this
}

@Composable
fun JsonRpcCallState.onError(block: @Composable (Int, String) -> Unit): JsonRpcCallState {
    if (this is JsonRpcCallState.ERROR) {
        block(this.code, this.message)
    }
    return this
}

@Composable
fun JsonRpcCallState.onLoading(block: @Composable () -> Unit): JsonRpcCallState {
  if (this == JsonRpcCallState.LOADING) {
    block()
  }
  return this
}
