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
import com.skydoves.moviecompose.addons.barcode.BarcodeAddon
import com.skydoves.moviecompose.addons.base.BaseAddon
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OdooAddonsModule {

    @Provides
    @Singleton
    fun provideBaseAddon(
        @ApplicationContext context: Context
    ): BaseAddon {
        return BaseAddon(context)
    }

    @Provides
    @Singleton
    fun provideBarcodeAddon(
        @ApplicationContext context: Context
    ): BarcodeAddon {
        return BarcodeAddon(context)
    }
}
