package com.xiaocydx.sample.viewpager2.animatable

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.xiaocydx.cxrv.list.Disposable

/**
 * 添加受父级[lifecycle]控制的[AnimatableController]
 *
 * @param state 当[lifecycle]的状态小于[state]时，调用[AnimatableMediator.stopAll]
 */
@Suppress("SpellCheckingInspection")
fun AnimatableMediator.controlledByLifecycle(lifecycle: Lifecycle, state: Lifecycle.State): Disposable {
    findController(RecyclerViewLifecycleController::class.java)?.dispose()
    return RecyclerViewLifecycleController(state).attach(this, lifecycle)
}

private class RecyclerViewLifecycleController(
    private val state: Lifecycle.State
) : LifecycleEventObserver, AnimatableController {
    private var mediator: AnimatableMediator? = null
    private var lifecycle: Lifecycle? = null
    override val isDisposed: Boolean
        get() = mediator == null && lifecycle == null
    override val isAllowStart: Boolean
        get() = lifecycle != null && lifecycle!!.currentState.isAtLeast(state)

    fun attach(
        mediator: AnimatableMediator,
        lifecycle: Lifecycle
    ): Disposable {
        this.mediator = mediator
        this.lifecycle = lifecycle
        mediator.addController(this)
        lifecycle.addObserver(this)
        return this
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val startEvent = Lifecycle.Event.upTo(state)
        val stopEvent = Lifecycle.Event.downFrom(state)
        when (event) {
            Lifecycle.Event.ON_DESTROY -> dispose()
            startEvent -> mediator?.startAll()
            stopEvent -> mediator?.stopAll()
            else -> return
        }
    }

    override fun dispose() {
        mediator?.removeController(this)
        lifecycle?.removeObserver(this)
        mediator = null
        lifecycle = null
    }
}