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

sealed class TaskExecuteState {
    object IDLE : TaskExecuteState()
    object LOADING : TaskExecuteState()
    object SUCCESS : TaskExecuteState()
    class ERROR(val code: Long, val message: String) : TaskExecuteState()
}

@Composable
fun TaskExecuteState.onSuccess(block: @Composable () -> Unit): TaskExecuteState {
  if (this == TaskExecuteState.SUCCESS) {
    block()
  }
  return this
}

@Composable
fun TaskExecuteState.onError(block: @Composable (Long, String) -> Unit): TaskExecuteState {
    if (this is TaskExecuteState.ERROR) {
        block(this.code, this.message)
    }
    return this
}

@Composable
fun TaskExecuteState.onLoading(block: @Composable () -> Unit): TaskExecuteState {
  if (this == TaskExecuteState.LOADING) {
    block()
  }
  return this
}
