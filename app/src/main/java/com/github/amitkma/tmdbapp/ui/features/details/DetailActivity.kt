package com.github.amitkma.tmdbapp.ui.features.details

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.github.amitkma.tmdbapp.R
import com.github.amitkma.tmdbapp.ServiceLocator
import com.github.amitkma.tmdbapp.data.vo.MovieDetail
import com.github.amitkma.tmdbapp.data.vo.TvDetail
import com.github.amitkma.tmdbapp.ui.gone
import com.github.amitkma.tmdbapp.ui.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_movie_detail.*
import timber.log.Timber

class DetailActivity : AppCompatActivity(), DetailView {

    private val detailStore: DetailStore by lazy { ServiceLocator.instance(this).detailStore }
    private val intentsSubject = PublishSubject.create<DetailStore.Intent>()
    override val intents = intentsSubject

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        setSupportActionBar(toolbar)
        title = ""
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        disposables += Observable.wrap(detailStore).subscribe { render(it) }
        disposables += intents.subscribe(detailStore)
        if (intent != null) {
            val id = intent.getIntExtra("ITEM_ID", 0)
            val type = intent.getStringExtra("ITEM_TYPE")
            dispatch(DetailStore.Intent.LoadDetailsIntent(id, type))
        } else {
            Timber.e(IllegalArgumentException("Intent data can't be null"))
        }
    }

    private fun dispatch(intent: DetailStore.Intent) {
        intentsSubject.onNext(intent)
    }

    override fun render(state: DetailStore.State) {
        when (state) {
            is DetailStore.State.LoadingDetailsState -> {
                errorText.gone()
                posterImage.gone()
                loadingBar.visible()
            }
            is DetailStore.State.ErrorLoadingDetailsState -> {
                errorText.visible()
                posterImage.gone()
                loadingBar.gone()
            }
            is DetailStore.State.ShowDetailsState -> {
                errorText.gone()
                loadingBar.gone()
                posterImage.visible()
                if (state.itemType == "tv") {
                    (state.detail as TvDetail)
                    state.detail.backdrop_path?.let {
                        posterImage.load(
                            ServiceLocator.instance(this).imageUrlProvider.getPosterUrl(
                                it,
                                posterImage.measuredWidth
                            )
                        )
                    }
                    titleText.text = state.detail.name
                    rating.text = "${state.detail.vote_average}/10"
                    releaseDateAndDurationText.text =
                        "${state.detail.first_air_date}, ${state.detail.episode_run_time} Min"
                    genreHolder.removeAllViews()
                    state.detail.genres.forEach {
                        val textView = LayoutInflater.from(this)
                            .inflate(R.layout.item_genre_chip, null) as TextView
                        textView.text = it.name
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        val margin = resources.getDimensionPixelSize(R.dimen.margin_chip)
                        layoutParams.setMargins(margin, 0, margin, 0)
                        genreHolder.addView(textView, layoutParams)
                    }
                    overviewText.text = state.detail.overview
                    if (state.cast.isNotEmpty()) {
                        castHeaderText.visible()
                        castRecyclerView.visible()
                        val adapter = CastProfileRecyclerAdapter(this)
                        castRecyclerView.setHasFixedSize(true)
                        castRecyclerView.layoutManager =
                            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                        castRecyclerView.adapter = adapter
                        adapter.items = state.cast
                    } else {
                        castHeaderText.gone()
                        castRecyclerView.gone()
                    }

                } else {
                    (state.detail as MovieDetail)
                    state.detail.backdrop_path?.let {
                        posterImage.load(
                            ServiceLocator.instance(this).imageUrlProvider.getPosterUrl(
                                it,
                                posterImage.measuredWidth
                            )
                        )
                    }
                    titleText.text = state.detail.title
                    rating.text = "${state.detail.vote_average}/10"
                    releaseDateAndDurationText.text =
                        "${state.detail.release_date}, ${state.detail.runtime} Min"
                    genreHolder.removeAllViews()
                    state.detail.genres.forEach {
                        val textView = LayoutInflater.from(this)
                            .inflate(R.layout.item_genre_chip, null) as TextView
                        textView.text = it.name
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        val margin = resources.getDimensionPixelSize(R.dimen.margin_chip)
                        layoutParams.setMargins(margin, 0, margin, 0)
                        genreHolder.addView(textView, layoutParams)
                    }
                    overviewText.text = state.detail.overview
                    if (state.cast.isNotEmpty()) {
                        castHeaderText.visible()
                        castRecyclerView.visible()
                        val adapter = CastProfileRecyclerAdapter(this)
                        castRecyclerView.setHasFixedSize(true)
                        castRecyclerView.layoutManager =
                            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                        castRecyclerView.adapter = adapter
                        adapter.items = state.cast
                    } else {
                        castHeaderText.gone()
                        castRecyclerView.gone()
                    }
                }
            }
        }
    }
}
