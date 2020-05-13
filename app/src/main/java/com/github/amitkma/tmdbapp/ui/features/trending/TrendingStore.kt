package com.github.amitkma.tmdbapp.ui.features.trending

import android.content.Context
import com.github.amitkma.tmdbapp.ServiceLocator
import com.github.amitkma.tmdbapp.data.vo.TrendingMovieTv
import com.github.amitkma.tmdbapp.data.vo.TrendingMovieTvResponse
import com.github.amitkma.tmdbapp.mvi.Actor
import com.github.amitkma.tmdbapp.mvi.IntentToAction
import com.github.amitkma.tmdbapp.mvi.NewsPublisher
import com.github.amitkma.tmdbapp.mvi.Reducer
import foundation.e.blisslauncher.mvicore.component.BaseStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class TrendingStore(context: Context) :
    BaseStore<TrendingStore.Intent,
            TrendingStore.Action,
            TrendingStore.Effect,
            TrendingStore.State,
            TrendingStore.News>(
        State.LoadingFirstPageState,
        IntentToActionImpl(),
        ActorImpl(context),
        ReducerImpl(),
        NewsPublisherImpl()
    ) {

    sealed class Intent {
        object InitialLoadIntent : Intent()
        object RefreshIntent : Intent()
        object LoadNextPageIntent : Intent()
        data class ViewMovieDetailsIntent(val trendingMovieTv: TrendingMovieTv) : Intent()
    }

    sealed class Action {
        object LoadFirstPage : Action()
        object LoadNextPage : Action()
        data class LoadMovieDetails(val trendingMovieTv: TrendingMovieTv) : Action()
    }

    sealed class Effect {
        data class StartedLoading(val loadingPage: Int) : Effect()
        data class LoadedResponse(val response: TrendingMovieTvResponse) : Effect()
        data class ErrorLoading(val throwable: Throwable) : Effect()
        data class OpenMovieDetails(val trendingMovieTv: TrendingMovieTv) : Effect()
    }

    sealed class State {

        object LoadingFirstPageState : State() {
            override fun toString(): String = LoadingFirstPageState::class.java.simpleName
        }

        data class ErrorLoadingFirstPageState(val errorMessage: String) : State() {
            override fun toString(): String =
                "${ErrorLoadingFirstPageState::class.java.simpleName} error=$errorMessage"
        }

        interface HasItems {
            val items: List<TrendingMovieTv>
            val page: Int
        }

        data class ShowContentState(
            override val items: List<TrendingMovieTv>,
            override val page: Int
        ) : State(), HasItems {
            override fun toString(): String =
                "${ShowContentState::class.java.simpleName} items=${items.size} page=$page"
        }

        data class ShowContentAndLoadNextPageState(
            override val items: List<TrendingMovieTv>,
            override val page: Int
        ) : State(), HasItems {
            override fun toString(): String =
                "${ShowContentAndLoadNextPageState::class.java.simpleName} items=${items.size} page=$page"
        }

        data class ShowContentAndLoadNextPageErrorState(
            override val items: List<TrendingMovieTv>,
            val errorMessage: String,
            override val page: Int
        ) : State(), HasItems {
            override fun toString(): String =
                "${ShowContentAndLoadNextPageErrorState::class.java.simpleName} error=$errorMessage items=${items.size}"
        }
    }

    // Nothing to do now
    sealed class News {
        data class DisplayMovieDetailActivity(val trendingMovieTv: TrendingMovieTv) : News()
    }


    class IntentToActionImpl : IntentToAction<Intent, Action> {
        override fun invoke(intent: Intent): Action = when (intent) {
            Intent.InitialLoadIntent -> Action.LoadFirstPage
            Intent.RefreshIntent -> Action.LoadFirstPage
            Intent.LoadNextPageIntent -> Action.LoadNextPage
            is Intent.ViewMovieDetailsIntent -> Action.LoadMovieDetails(intent.trendingMovieTv)
        }
    }

    class ActorImpl(context: Context) : Actor<State, Action, Effect> {

        private val repository = ServiceLocator.instance(context).getRepository()

        override fun invoke(state: State, action: Action): Observable<out Effect> {
            return when (action) {
                is Action.LoadFirstPage -> Observable.just(Unit)
                    .filter { state !is State.HasItems }
                    .flatMap { repository.getTrendingMoviesOrShows(1).toObservable() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { Effect.LoadedResponse(it) as Effect }
                    .startWith(Effect.StartedLoading(1))
                    .onErrorReturn { Effect.ErrorLoading(it) }

                Action.LoadNextPage -> {
                    val nextPage = (if (state is State.HasItems) state.page else 0) + 1
                    repository.getTrendingMoviesOrShows(nextPage)
                        .toObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map { Effect.LoadedResponse(it) as Effect }
                        .startWith(Effect.StartedLoading(nextPage))
                        .onErrorReturn { Effect.ErrorLoading(it) }
                }

                is Action.LoadMovieDetails -> Observable.just(Effect.OpenMovieDetails(action.trendingMovieTv))
            }
        }

    }

    class ReducerImpl : Reducer<State, Effect> {

        override fun invoke(state: State, effect: Effect): State =
            when (effect) {
                is Effect.StartedLoading -> {
                    if (state is State.HasItems && state.page >= 1 && effect.loadingPage != state.page) {
                        State.ShowContentAndLoadNextPageState(state.items, state.page)
                    } else if (state is State.HasItems) {
                        state
                    } else State.LoadingFirstPageState
                }
                is Effect.LoadedResponse -> {
                    val items =
                        if (state is State.HasItems) state.items + effect.response.results
                        else effect.response.results
                    val page = effect.response.page
                    State.ShowContentState(items, page)
                }
                is Effect.ErrorLoading -> {
                    if (state is State.HasItems) {
                        State.ShowContentAndLoadNextPageErrorState(
                            state.items,
                            effect.throwable.localizedMessage,
                            state.page
                        )
                    } else {
                        State.ErrorLoadingFirstPageState(effect.throwable.localizedMessage)
                    }
                }
                is Effect.OpenMovieDetails -> state
            }
    }

    class NewsPublisherImpl : NewsPublisher<Action, Effect, State, News> {
        override fun invoke(action: Action, effect: Effect, state: State): News? =
            when (effect) {
                is Effect.OpenMovieDetails -> News.DisplayMovieDetailActivity(effect.trendingMovieTv)
                else -> null
            }

    }
}