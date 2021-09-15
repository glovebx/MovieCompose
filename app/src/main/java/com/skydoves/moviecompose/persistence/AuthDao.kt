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

package com.skydoves.moviecompose.persistence

import androidx.room.*
import com.skydoves.moviecompose.models.entities.OdooAuthenticate

@Dao
interface AuthDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOdooAuthenticate(odooAuthenticate: OdooAuthenticate)

  @Update
  suspend fun updateOdooAuthenticate(odooAuthenticate: OdooAuthenticate)

  @Query("SELECT * FROM OdooAuthenticate WHERE active == 1")
  suspend fun getCurrentOdooAuthenticate(): OdooAuthenticate

  @Query("SELECT * FROM OdooAuthenticate ORDER BY active DESC")
  suspend fun getOdooAuthenticateList(): List<OdooAuthenticate>

  @Query("SELECT * FROM OdooAuthenticate WHERE serverUrl = :serverUrl_")
  suspend fun getOdooDatabaseList(serverUrl_: String): List<OdooAuthenticate>
}
