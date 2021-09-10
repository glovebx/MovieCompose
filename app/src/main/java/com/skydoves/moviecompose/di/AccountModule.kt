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

import android.accounts.AccountManager
import android.content.Context
import com.skydoves.moviecompose.accounts.OdooAccountManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

//    @Provides
//    @Singleton
//    fun provideAccountManager(@ApplicationContext context: Context): AccountManager {
//        return AccountManager.get(context)
//    }

    @Provides
    @Singleton
    fun provideOdooAccountManager(
        @ApplicationContext context: Context
    ): OdooAccountManager {
        return OdooAccountManager(context, AccountManager.get(context))
    }
}
