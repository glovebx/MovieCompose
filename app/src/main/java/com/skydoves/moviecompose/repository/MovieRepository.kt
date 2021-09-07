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

package com.skydoves.moviecompose.repository

import androidx.annotation.WorkerThread
import com.skydoves.moviecompose.models.Keyword
import com.skydoves.moviecompose.models.Review
import com.skydoves.moviecompose.models.Video
import com.skydoves.moviecompose.models.network.MovieResult
import com.skydoves.moviecompose.network.service.MovieService
import com.skydoves.moviecompose.persistence.MovieDao
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class MovieRepository constructor(
  private val movieService: MovieService,
  private val movieDao: MovieDao
) : Repository {

  init {
    Timber.d("Injection MovieRepository")
  }

  @WorkerThread
  fun loadKeywordList(url: String) = flow<List<Keyword>> {
//    val movie = movieDao.getMovie(id)
//    var keywords = movie.keywords
//    if (keywords.isNullOrEmpty()) {
      val response = movieService.fetchKeywords(url)
      response.suspendOnSuccess {
        var keywords = data.keywords
//        movie.keywords = keywords
//        movieDao.updateMovie(movie)
        emit(keywords ?: listOf())
      }
//    } else {
//      emit(keywords ?: listOf())
//    }
  }.flowOn(Dispatchers.IO)

  @WorkerThread
  fun loadVideoList(url: String) = flow<List<Video>> {
//    val movie = movieDao.getMovie(id)
//    var videos = movie.videos
//    if (videos.isNullOrEmpty()) {
      movieService.fetchVideos(url)
        .suspendOnSuccess {
          var videos = data.results
//          movie.videos = videos
//          movieDao.updateMovie(movie)
          emit(videos ?: listOf())
        }
//    } else {
//      emit(videos ?: listOf())
//    }
  }.flowOn(Dispatchers.IO)

  @WorkerThread
  fun loadReviewsList(url: String) = flow<List<Review>> {
//    val movie = movieDao.getMovie(id)
//    var reviews = movie.reviews
//    if (reviews.isNullOrEmpty()) {
      movieService.fetchReviews(url)
        .suspendOnSuccess {
          var reviews = data.results
//          movie.reviews = reviews
//          movieDao.updateMovie(movie)
          emit(reviews ?: listOf())
        }
//    } else {
//      emit(reviews ?: listOf())
//    }
  }.flowOn(Dispatchers.IO)

  @WorkerThread
  fun loadMovieByUrl(url5: String) = flow {
    val movie = movieDao.getMovie(url5)
//    emit(movie)
    val params = mapOf("url" to movie.url)
    val response = movieService.fetchMovie(params)
    response.suspendOnSuccess {
      if (data.error != null) {
        // 从error里面抽取出错误消息
        emit(MovieResult(data.error!!.code, data.error!!.message))
      } else {
        emit(data.result!!)
      }
    }
  }.flowOn(Dispatchers.IO)
}
