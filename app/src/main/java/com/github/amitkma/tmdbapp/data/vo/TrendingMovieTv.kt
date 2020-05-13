package com.github.amitkma.tmdbapp.data.vo

import com.github.amitkma.tmdbapp.data.config.Genres
import timber.log.Timber

data class TrendingMovieTv(
    val poster_path: String?,
    val adult: Boolean,
    val overview: String,
    val release_date: String,
    val id: Int,
    val original_title: String,
    val original_language: String,
    val title: String,
    val backdrop_path: String?,
    val popularity: Double,
    val vote_count: Int,
    val vote_average: Double,
    val media_type: String,
    val name: String,
    val original_name: String,
    val genre_ids: List<Int>
) {
    fun genres(): List<String> {
        val list = ArrayList<String>()
        genre_ids.forEach {
            list.add(Genres.all[it]!!)
        }
        Timber.d("$list")
        return list
    }
}

data class TrendingMovieTvResponse(
    val page: Int,
    val results: List<TrendingMovieTv>,
    val total_pages: Int,
    val total_results: Int
)