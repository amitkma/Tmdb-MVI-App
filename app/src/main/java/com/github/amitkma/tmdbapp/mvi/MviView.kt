package foundation.e.blisslauncher.mvicore.component

import io.reactivex.Observable
import io.reactivex.functions.Consumer

interface MviView<in ViewState : Any, Intent : Any> {

    val intents: Observable<Intent>

    fun render(state: ViewState)
}