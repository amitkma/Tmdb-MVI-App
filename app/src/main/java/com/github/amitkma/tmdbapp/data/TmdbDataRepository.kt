package com.github.amitkma.tmdbapp.data

import android.content.Context
import com.github.amitkma.tmdbapp.ServiceLocator
import com.github.amitkma.tmdbapp.data.api.TmdbApi
import com.github.amitkma.tmdbapp.data.vo.Cast
import com.github.amitkma.tmdbapp.data.vo.MovieDetail
import com.github.amitkma.tmdbapp.data.vo.TrendingMovieTvResponse
import com.github.amitkma.tmdbapp.data.vo.TvDetail
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class TmdbDataRepository(private val context: Context, private val tmdbApi: TmdbApi) {

    fun getTrendingMoviesOrShows(page: Int): Single<TrendingMovieTvResponse> =
        tmdbApi.getTrendingMoviesOrShows(page = page)
            .subscribeOn(Schedulers.from(ServiceLocator.instance(context).getNetworkExecutor()))

    fun getMovieDetails(movieId: Int): Single<MovieDetail> =
        tmdbApi.getMovieDetails(movieId)
            .subscribeOn(Schedulers.from(ServiceLocator.instance(context).getNetworkExecutor()))

    fun getMovieCast(movieId: Int): Single<List<Cast>> = tmdbApi.getMovieCredits(movieId)
        .map { it.cast }
        .subscribeOn(Schedulers.from(ServiceLocator.instance(context).getNetworkExecutor()))

    fun getTvDetails(tvId: Int): Single<TvDetail> =
        tmdbApi.getTvDetails(tvId)
            .subscribeOn(Schedulers.from(ServiceLocator.instance(context).getNetworkExecutor()))

    fun getTvCast(tvId: Int): Single<List<Cast>> = tmdbApi.getTVCredits(tvId)
        .map { it.cast }
        .subscribeOn(Schedulers.from(ServiceLocator.instance(context).getNetworkExecutor()))
}