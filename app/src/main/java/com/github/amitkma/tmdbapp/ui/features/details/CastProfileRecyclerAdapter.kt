package com.github.amitkma.tmdbapp.ui.features.details

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
import com.github.amitkma.tmdbapp.data.vo.Cast
import com.github.amitkma.tmdbapp.data.vo.TrendingMovieTv


class CastProfileRecyclerAdapter(
    private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items = emptyList<Cast>()

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = CastItemViewHolder(
        layoutInflater.inflate(R.layout.item_cast, parent,false)
    )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CastItemViewHolder) {
            holder.bind(items[position])
        }
    }

    inner class CastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val profile: ImageView = view.findViewById<ImageView>(R.id.castProfileImage)
        private val name: TextView = view.findViewById<TextView>(R.id.castProfileName)


        fun bind(cast: Cast) {
            name.text = cast.name
            cast.profile_path?.let {
                profile.load(
                    ServiceLocator.instance(context).imageUrlProvider.getPosterUrl(
                        it,context.resources.getDimensionPixelSize(R.dimen.cast_profile_size)
                    )
                )
            }
        }


    }
}