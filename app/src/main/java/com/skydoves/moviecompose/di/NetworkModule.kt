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

package com.skydoves.moviecompose.di

import android.content.Context
import coil.ImageLoader
import com.skydoves.moviecompose.BuildConfig
import com.skydoves.moviecompose.network.Api
import com.skydoves.moviecompose.network.HostInterceptor
import com.skydoves.moviecompose.network.RequestInterceptor
import com.skydoves.moviecompose.network.service.*
import com.skydoves.moviecompose.sandwich.JsonRpcConverterFactory
import com.skydoves.sandwich.coroutines.CoroutinesResponseCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HostInterceptor())
      .addInterceptor(RequestInterceptor())
      .addInterceptor(HttpLoggingInterceptor().setLevel(
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE)
      ).build()
  }

  @Provides
  @Singleton
  fun provideImageLoader(
    @ApplicationContext context: Context,
    okHttpClient: OkHttpClient
  ): ImageLoader {
    return ImageLoader.Builder(context)
      .okHttpClient { okHttpClient }
      .build()
  }

  @Provides
  @Singleton
  fun provideRetrofit(okhHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .client(okhHttpClient)
      .baseUrl(Api.BASE_URL)
      .addConverterFactory(JsonRpcConverterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(CoroutinesResponseCallAdapterFactory.create())
      .build()
  }

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

  @Provides
  @Singleton
  fun provideTheDiscoverService(retrofit: Retrofit): TheDiscoverService {
    return retrofit.create(TheDiscoverService::class.java)
  }

  @Provides
  @Singleton
  fun provideMovieService(retrofit: Retrofit): MovieService {
    return retrofit.create(MovieService::class.java)
  }

  @Provides
  @Singleton
  fun provideTvService(retrofit: Retrofit): TvService {
    return retrofit.create(TvService::class.java)
  }

  @Provides
  @Singleton
  fun providePeopleService(retrofit: Retrofit): PeopleService {
    return retrofit.create(PeopleService::class.java)
  }
}
