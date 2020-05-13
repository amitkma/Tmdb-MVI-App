package com.github.amitkma.tmdbapp.data.api

import com.github.amitkma.tmdbapp.data.vo.Credits
import com.github.amitkma.tmdbapp.data.vo.MovieDetail
import com.github.amitkma.tmdbapp.data.vo.TrendingMovieTvResponse
import com.github.amitkma.tmdbapp.data.vo.TvDetail
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import timber.log.Timber

interface TmdbApi {

    @GET("trending/{media_type}/{time_window}")
    fun getTrendingMoviesOrShows(
        @Path("media_type") filter: String = "all",
        @Path("time_window") timeWindow: String = "day",
        @Query("page") page: Int? = 1
    ): Single<TrendingMovieTvResponse>

    @GET("movie/{movie_id}")
    fun getMovieDetails(@Path("movie_id") movieId: Int): Single<MovieDetail>

    @GET("tv/{tv_id}")
    fun getTvDetails(@Path("tv_id") tvId: Int): Single<TvDetail>

    @GET("movie/{movie_id}/credits")
    fun getMovieCredits(@Path("movie_id") movieId: Int): Single<Credits>

    @GET("tv/{tv_id}/credits")
    fun getTVCredits(@Path("tv_id") tvId: Int): Single<Credits>

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"
        private const val API_KEY = "761132fa9ad919c897319f352aa87b40"
        fun create(baseUrl: String = BASE_URL): TmdbApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Timber.d(it)
            })

            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(Interceptor { chain ->
                    val originalRequest = chain.request()
                    val httpUrl = originalRequest.url().newBuilder()
                        .addQueryParameter("api_key", API_KEY)
                        .build()
                    Timber.d("URL: $httpUrl")
                    Timber.d("URL: ${originalRequest.url()}")
                    chain.proceed(originalRequest.newBuilder().url(httpUrl).build())
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TmdbApi::class.java)
        }
    }
}