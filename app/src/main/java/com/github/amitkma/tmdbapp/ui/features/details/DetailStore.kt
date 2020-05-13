package com.github.amitkma.tmdbapp.ui.features.details

import android.content.Context
import com.github.amitkma.tmdbapp.ServiceLocator
import com.github.amitkma.tmdbapp.data.vo.BaseTvMovieDetail
import com.github.amitkma.tmdbapp.data.vo.Cast
import com.github.amitkma.tmdbapp.mvi.Actor
import com.github.amitkma.tmdbapp.mvi.IntentToAction
import com.github.amitkma.tmdbapp.mvi.Reducer
import com.github.amitkma.tmdbapp.mvi.SideEffect
import foundation.e.blisslauncher.mvicore.component.BaseStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

val sideEffect: SideEffect<DetailStore.Action, DetailStore.Effect, DetailStore.State> =
    { _, effect, state ->
        run {
            if (state is DetailStore.State.ShowDetailsState && effect is DetailStore.Effect.LoadedDetails<*>)
                DetailStore.Action.LoadCastAction(state.itemId, state.itemType)
            else null
        }
    }

class DetailStore(context: Context) :
    BaseStore<DetailStore.Intent,
            DetailStore.Action,
            DetailStore.Effect,
            DetailStore.State,
            DetailStore.News>(
        State.LoadingDetailsState(),
        IntentToActionImpl(),
        ActorImpl(context),
        ReducerImpl(),
        sideEffect = sideEffect
    ) {
    sealed class Intent {
        data class LoadDetailsIntent(val itemId: Int, val itemType: String) : Intent()
    }

    sealed class Action {
        data class LoadDetailsAction(val itemId: Int, val itemType: String) : Action()
        data class LoadCastAction(val itemId: Int, val itemType: String) : Action()
        data class LoadSimilarAction(val itemId: Int, val itemType: String) : Action()
    }

    sealed class Effect {
        data class StartedLoading(val itemId: Int, val itemType: String) : Effect()
        data class ErrorLoadingDetails(val throwable: Throwable) : Effect()
        data class LoadedDetails<T: BaseTvMovieDetail>(val details: T) : Effect()
        data class LoadedCast(val cast: List<Cast>) : Effect()
        data class ErrorLoadingSubDetails(val throwable: Throwable) : Effect()
    }

    sealed class State {
        abstract val itemId: Int
        abstract val itemType: String

        data class LoadingDetailsState(
            override val itemId: Int = 0,
            override val itemType: String = ""
        ) :
            State()

        data class ErrorLoadingDetailsState(
            override val itemId: Int,
            override val itemType: String
        ) : State()

        data class ShowDetailsState(
            override val itemId: Int,
            override val itemType: String,
            val detail: BaseTvMovieDetail,
            val cast: List<Cast> = emptyList()
        ) : State()
    }

    sealed class News

    class IntentToActionImpl : IntentToAction<Intent, Action> {
        override fun invoke(intent: Intent): Action = when (intent) {
            is Intent.LoadDetailsIntent -> Action.LoadDetailsAction(intent.itemId, intent.itemType)
        }
    }

    class ActorImpl(private val context: Context) : Actor<State, Action, Effect> {
        private val repository = ServiceLocator.instance(context).getRepository()

        override fun invoke(state: State, action: Action): Observable<out Effect> = when (action) {
            is Action.LoadDetailsAction -> {
                val single =
                    if (action.itemType == "tv") repository.getTvDetails(action.itemId)
                    else repository.getMovieDetails(action.itemId)
                single.toObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { Effect.LoadedDetails(it) as Effect }
                    .startWith(Effect.StartedLoading(action.itemId, action.itemType))
                    .onErrorReturn { Effect.ErrorLoadingDetails(it) }
            }
            is Action.LoadCastAction -> {
                val single =
                    if (action.itemType == "tv") repository.getTvCast(action.itemId)
                    else repository.getMovieCast(action.itemId)
                single.toObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { Effect.LoadedCast(it) as Effect }
                    .onErrorReturn { Effect.ErrorLoadingSubDetails(it) }

            }
            is Action.LoadSimilarAction -> {
                TODO()
            }
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = when (effect) {
            is Effect.StartedLoading -> State.LoadingDetailsState(effect.itemId, effect.itemType)
            is Effect.LoadedDetails<*> -> {
                State.ShowDetailsState(state.itemId, state.itemType, effect.details)
            }
            is Effect.ErrorLoadingDetails -> State.ErrorLoadingDetailsState(
                state.itemId,
                state.itemType
            )
            is Effect.LoadedCast ->
                if (state is State.ShowDetailsState)
                    state.copy(cast = effect.cast)
                else throw IllegalStateException("Can't load cast without loading details")

            is Effect.ErrorLoadingSubDetails -> {
                Timber.e(effect.throwable)
                state
            }
        }
    }
}