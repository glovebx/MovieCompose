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

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skydoves.moviecompose.models.Image
import com.skydoves.moviecompose.models.Keyword
import com.skydoves.moviecompose.models.Review
import com.skydoves.moviecompose.models.Video

@Immutable
@Entity//(primaryKeys = [("id")])
data class Movie(
  @PrimaryKey(autoGenerate = true)
  val id: Long,
  var page: Int,

  val title: String,
  val coverUrl: String,
  val url: String,
  val desc: String,
  val price: String,
  val currency: String,
  val authorName: String,
  val authorWebsite: String,
  val verified: String,
  val voteAverage: Float,
  val releaseDate: String?,

  var technicalName: String?,
  var license: String?,
  var downloadNum: Int,
  var availableVersions: List<String>? = ArrayList(),
  var descImages: List<String>? = ArrayList(),

  var keywords: List<Keyword>? = ArrayList(),
  var videos: List<Video>? = ArrayList(),
  var images: List<Image>? = ArrayList(),
  var reviews: List<Review>? = ArrayList(),
)
