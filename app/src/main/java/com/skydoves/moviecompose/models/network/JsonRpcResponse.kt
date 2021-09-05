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

import androidx.compose.runtime.Immutable
import com.skydoves.moviecompose.models.NetworkResponseModel

@Immutable
class JsonRpcResponse<T> : NetworkResponseModel {
  var id: Long = 0
  var result: T? = null
  var error: JsonRpcError? = null

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false
    val that = o as JsonRpcResponse<*>
    if (id != that.id) return false
    return if (if (result != null) result != that.result else that.result != null) false else !if (error != null) error != that.error else that.error != null
  }

  override fun hashCode(): Int {
    var result1 = (id xor (id ushr 32)).toInt()
    result1 = 31 * result1 + if (result != null) result.hashCode() else 0
    result1 = 31 * result1 + if (error != null) error.hashCode() else 0
    return result1
  }
}
