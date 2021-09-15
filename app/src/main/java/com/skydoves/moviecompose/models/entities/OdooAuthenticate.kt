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

package com.skydoves.moviecompose.models.entities

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Immutable
@Entity(primaryKeys = ["serverUrl", "db", "uid"])
data class OdooAuthenticate(
  val uid: Long,
  val db: String,
  val name: String,
  val username: String,
  @SerializedName("partner_display_name")
  val partnerDisplayName: String?,
  @SerializedName("company_id")
  val companyId: Int,
  @SerializedName("partner_id")
  val partnerId: Long,
  @SerializedName("web.base.url")
  val webBaseUrl: String?,
  @SerializedName("user_context")
  @Embedded val userContext: OdooContext,
  @SerializedName("server_version")
  val serverVersion: String,

  // 是否当前账号
  var active: Int = 0,

  @NonNull var serverUrl: String,
  // 保留，本地不保存
  var password: String?,
  var sessionId: String?
) {

  fun afterAuthenticated(serverUrl: String, sessionId: String?) {
    this.serverUrl = serverUrl
    this.sessionId = sessionId
    this.active = 1
  }
//
//  override fun equals(other: Any?): Boolean {
//    if (this === other) return true
//    if (javaClass != other?.javaClass) return false
//
//    other as OdooAuthenticate
//
//    if (serverUrl != other.serverUrl) return false
//    if (db != other.db) return false
//    if (uid != other.uid) return false
//
//    return true
//  }

}
