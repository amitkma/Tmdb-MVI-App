package com.github.amitkma.tmdbapp.data.vo

data class MovieDetail(
    val adult: Boolean,
    val release_date: String,
    val original_title: String,
    val original_language: String,
    val title: String,
    val tagline: String,
    val runtime: Int?,
    val backdrop_path: String?,
    val vote_average: Double,
    val vote_count: Int,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val id: Int,
    val genres: List<Genre>,
    val homepage: String
): BaseTvMovieDetail