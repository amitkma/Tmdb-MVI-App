package com.github.amitkma.tmdbapp.ui.features.trending

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.github.amitkma.tmdbapp.R
import com.github.amitkma.tmdbapp.ServiceLocator
import com.github.amitkma.tmdbapp.data.vo.TrendingMovieTv

const val VIEW_TYPE_ITEM = 101
const val VIEW_TYPE_LOADING_NEXT_PAGE = 102

class TrendingRecyclerViewAdapter(
    private val context: Context,
    private val movieClickListener: (TrendingMovieTv) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items = emptyList<TrendingMovieTv>()
    var showLoading = false

    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int =
        if (showLoading && position == items.size)
            VIEW_TYPE_LOADING_NEXT_PAGE
        else
            VIEW_TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_ITEM -> TrendingItemsViewHolder(
                layoutInflater.inflate(
                    R.layout.item_trending_movie,
                    parent,
                    false
                )
            )
            VIEW_TYPE_LOADING_NEXT_PAGE -> LoadingNextPageViewHolder(
                layoutInflater.inflate(
                    R.layout.item_load_next,
                    parent,
                    false
                )
            )
            else -> throw IllegalAccessException("$viewType is not an expected View type")
        }

    override fun getItemCount(): Int = items.size + (if (showLoading) 1 else 0)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TrendingItemsViewHolder) {
            holder.bind(items[position])
        }
    }

    inner class TrendingItemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val poster: ImageView = view.findViewById<ImageView>(R.id.posterImage)
        private val title: TextView = view.findViewById<TextView>(R.id.titleText)
        private val genres: TextView = view.findViewById<TextView>(R.id.genreText)
        private val overview: TextView = view.findViewById<TextView>(R.id.overviewText)

        init {
            view.setOnClickListener {
                movieClickListener.invoke(items[adapterPosition])
            }
        }

        fun bind(trendingMovieTv: TrendingMovieTv) {
            title.text =
                if (trendingMovieTv.media_type == "movie") trendingMovieTv.title else trendingMovieTv.name
            genres.text = trendingMovieTv.genres().take(3).joinToString(separator = " | ")
            overview.text = trendingMovieTv.overview
            trendingMovieTv.poster_path?.let {
                poster.load(
                    ServiceLocator.instance(context).imageUrlProvider.getPosterUrl(
                        trendingMovieTv.poster_path,
                        1280
                    )
                )
            }
        }


    }

    inner class LoadingNextPageViewHolder(view: View) : RecyclerView.ViewHolder(view)
}