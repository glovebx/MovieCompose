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

package com.skydoves.moviecompose

import android.app.Application
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MovieComposeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val params = mapOf(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER to true,
            TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE to true)
        QbSdk.initTbsSettings(params)

        QbSdk.initX5Environment(applicationContext, object: QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
//                TODO("Not yet implemented")
                Timber.d("onCoreInitFinished")
            }

            override fun onViewInitFinished(p0: Boolean) {
//                TODO("Not yet implemented")
                Timber.d("onViewInitFinished")
            }

        })
    }
}
