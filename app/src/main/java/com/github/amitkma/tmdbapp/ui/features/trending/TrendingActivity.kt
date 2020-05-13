package com.github.amitkma.tmdbapp.ui.features.trending

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.amitkma.tmdbapp.R
import com.github.amitkma.tmdbapp.ServiceLocator
import com.github.amitkma.tmdbapp.data.vo.TrendingMovieTv
import com.github.amitkma.tmdbapp.ui.features.details.DetailActivity
import com.github.amitkma.tmdbapp.ui.gone
import com.github.amitkma.tmdbapp.ui.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_trending.*
import timber.log.Timber

class TrendingActivity : AppCompatActivity(), TrendingView {

    private val trendingStore: TrendingStore by lazy {
        ServiceLocator.instance(this).trendingStore
    }

    private val intentsSubject = PublishSubject.create<TrendingStore.Intent>()
    override val intents = intentsSubject

    private val disposables = CompositeDisposable()

    private val movieClickListener: (TrendingMovieTv) -> Unit =
        { movie -> dispatch(TrendingStore.Intent.ViewMovieDetailsIntent(movie)) }
    private val adapter: TrendingRecyclerViewAdapter by lazy {
        TrendingRecyclerViewAdapter(this, movieClickListener)
    }
    private val linearLayoutManager = LinearLayoutManager(this)

    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                linearLayoutManager.findFirstVisibleItemPosition() == 0 &&
                linearLayoutManager.findViewByPosition(0)!!.top == trendingRecyclerView.paddingTop &&
                toolbar.translationZ != 0f
            ) {
                // at top, reset elevation
                toolbar.translationZ = 0f
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING && toolbar.translationZ != -1f) {
                // grid scrolled, lower toolbar to allow content to pass in front
                toolbar.translationZ = -1f
            }

            val endReached = !recyclerView.canScrollVertically(1)
            Timber.d("Scroll changed: $endReached")
            if (endReached) {
                dispatch(TrendingStore.Intent.LoadNextPageIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trending)

        trendingRecyclerView.layoutManager = linearLayoutManager
        trendingRecyclerView.setHasFixedSize(true)
        trendingRecyclerView.adapter = adapter
        trendingRecyclerView.addOnScrollListener(listener)

        disposables += Observable.wrap(trendingStore).subscribe { render(it) }
        disposables += Observable.wrap(trendingStore.news).subscribe { processNews(it) }
        disposables += intents.subscribe(trendingStore)

        setSupportActionBar(toolbar)
        title = "Trending"
        dispatch(TrendingStore.Intent.InitialLoadIntent)
    }

    private fun dispatch(intent: TrendingStore.Intent) {
        intentsSubject.onNext(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        trendingRecyclerView.removeOnScrollListener(listener)
    }

    override fun render(state: TrendingStore.State) {
        when (state) {
            TrendingStore.State.LoadingFirstPageState -> {
                trendingRecyclerView.gone()
                loadingBar.visible()
                errorText.gone()
            }
            is TrendingStore.State.ShowContentState -> {
                trendingRecyclerView.visible()
                loadingBar.gone()
                errorText.gone()
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
            is TrendingStore.State.ShowContentAndLoadNextPageState -> {
                trendingRecyclerView.visible()
                loadingBar.gone()
                errorText.gone()
                adapter.items = state.items
                adapter.showLoading = true
                adapter.notifyDataSetChanged()
            }

            is TrendingStore.State.ShowContentAndLoadNextPageErrorState -> {
                trendingRecyclerView.visible()
                loadingBar.gone()
                errorText.gone()
                adapter.showLoading = false
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT).show()
            }

            is TrendingStore.State.ErrorLoadingFirstPageState -> {
                trendingRecyclerView.gone()
                loadingBar.gone()
                errorText.visible()
            }
        }
    }

    private fun processNews(news: TrendingStore.News) {
        when (news) {
            is TrendingStore.News.DisplayMovieDetailActivity -> {
                val intent = android.content.Intent(this, DetailActivity::class.java)
                intent.putExtra("ITEM_ID", news.trendingMovieTv.id)
                intent.putExtra("ITEM_TYPE", news.trendingMovieTv.media_type)
                startActivity(intent)
            }
        }
    }
}