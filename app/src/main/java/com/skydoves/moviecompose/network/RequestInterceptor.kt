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

package com.skydoves.moviecompose.network

import com.skydoves.moviecompose.BuildConfig
import com.skydoves.moviecompose.accounts.OdooManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

internal const val FAKE_BASE_URL = Api.BASE_URL

internal class HostInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (OdooManager.serverUrl.isNullOrEmpty() || !request.url.toString().startsWith(FAKE_BASE_URL, ignoreCase = true)) {
            return chain.proceed(request)
        }
        // 没有定义完整的url，替换odoo
        return chain.proceed(
            request.newBuilder()
                .url(
                    request.url.toString().replace(FAKE_BASE_URL, OdooManager.serverUrl!!)
                        .toHttpUrlOrNull() ?: request.url
                )
                .build()
        )
    }
}

internal class RequestInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val originalUrl = originalRequest.url
    val url = originalUrl.newBuilder()
      .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
      .build()

    val requestBuilder = originalRequest.newBuilder().url(url)
    val request = requestBuilder.build()
    return chain.proceed(request)
  }
}
