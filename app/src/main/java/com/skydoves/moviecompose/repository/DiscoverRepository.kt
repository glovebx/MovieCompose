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
import com.skydoves.moviecompose.models.network.DiscoverMovieResult
import com.skydoves.moviecompose.network.service.TheDiscoverService
import com.skydoves.moviecompose.persistence.MovieDao
import com.skydoves.moviecompose.persistence.TvDao
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onException
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import timber.log.Timber

class DiscoverRepository constructor(
  private val discoverService: TheDiscoverService,
  private val movieDao: MovieDao,
  private val tvDao: TvDao
) : Repository {

  init {
    Timber.d("Injection DiscoverRepository")
  }

  @WorkerThread
  fun loadMovies(page: Int, success: () -> Unit, error: () -> Unit) = flow {
//    var movies = movieDao.getMovieList(page)
//    if (movies.isEmpty()) {
      val params = mapOf("page" to page.toString())
      val response = discoverService.fetchDiscoverMovie(params)
      response.suspendOnSuccess {
        // 确保list不能是null
        data?.list.run {
          movieDao.insertMovieList(this)
        }
//        data.result?.run {
//          var movies = this.list
////          movies.forEach { it.page = page }
//          movieDao.insertMovieList(movies)
//        }
//          if (data.error != null) {
//              // 从error里面抽取出错误消息
//              emit(DiscoverMovieResult(data.error!!.code, data.error!!.message))
//          } else {
//              emit(data.result!!)
//          }
        emit(data)
      }.onError {
        error()
      }.onException { error() }
//    } else {
//      emit(movies)
//    }
//
//    val params = mapOf("db" to "odoo14e", "login" to "admin", "password" to "admin")
//    val response = discoverService.authenticate(params)
//    response.suspendOnSuccess {
//      emit(data)
//    }.onError {
//      error()
//    }.onException { error() }
  }.onCompletion { success() }.flowOn(Dispatchers.IO)

  @WorkerThread
  fun loadTvs(page: Int, success: () -> Unit, error: () -> Unit) = flow {
    var tvs = tvDao.getTvList(page)
    if (tvs.isEmpty()) {
      val response = discoverService.fetchDiscoverTv(page)
      response.suspendOnSuccess {
        tvs = data.results
        tvs.forEach { it.page = page }
        tvDao.insertTv(tvs)
        emit(tvs)
      }.onError {
        error()
      }.onException { error() }
    } else {
      emit(tvs)
    }
  }.onCompletion { success() }.flowOn(Dispatchers.IO)
}
