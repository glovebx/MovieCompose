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

package com.skydoves.moviecompose.network.service

import com.skydoves.moviecompose.models.entities.Database
import com.skydoves.moviecompose.models.entities.OdooAuthenticate
import com.skydoves.moviecompose.models.entities.Version
import com.skydoves.moviecompose.sandwich.JsonRpcCall
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/web/session/get_session_info")
    @JsonRpcCall
    @JvmSuppressWildcards
    suspend fun fetchSessionInfo(@Body params: Map<String, Any>): ApiResponse<OdooAuthenticate>

    @POST("/web/webclient/version_info")
    @JsonRpcCall
    @JvmSuppressWildcards
    suspend fun fetchVersionInfo(@Body params: Map<String, Any>): ApiResponse<Version>

    @POST("/web/database/list")
    @JsonRpcCall
    @JvmSuppressWildcards
    suspend fun fetchDatabaseList(@Body params: Map<String, Any>): ApiResponse<List<Database>>

    @POST("/web/session/authenticate")
    @JsonRpcCall
    @JvmSuppressWildcards
    suspend fun authenticate(@Body params: Map<String, Any>): ApiResponse<OdooAuthenticate>
}
