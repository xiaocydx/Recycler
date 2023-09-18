@file:Suppress("PackageDirectoryMismatch")
@file:JvmName("FragmentTransitionCompat21EnhanceInternal")

package androidx.fragment.app

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Looper
import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.core.os.CancellationSignal
import java.lang.ref.WeakReference

/**
 * 增强[FragmentTransitionCompat21]的兼容处理
 *
 * 调用[beginDelayedTransition]时，优化过渡动画的捕获流程，达到仅捕获`target`的效果。
 *
 * @author xcc
 * @date 2023/9/18
 */
@SuppressLint("RestrictedApi")
internal class FragmentTransitionCompat21Enhance(
    private val delegate: FragmentTransitionImpl
) : FragmentTransitionImpl() {

    override fun canHandle(transition: Any): Boolean {
        return delegate.canHandle(transition)
    }

    override fun cloneTransition(transition: Any?): Any {
        return delegate.cloneTransition(transition)
    }

    override fun wrapTransitionInSet(transition: Any?): Any {
        return delegate.wrapTransitionInSet(transition)
    }

    override fun setSharedElementTargets(transitionObj: Any, nonExistentView: View, sharedViews: ArrayList<View>) {
        delegate.setSharedElementTargets(transitionObj, nonExistentView, sharedViews)
    }

    override fun setEpicenter(transitionObj: Any, view: View?) {
        delegate.setEpicenter(transitionObj, view)
    }

    override fun setEpicenter(transitionObj: Any, epicenter: Rect) {
        delegate.setEpicenter(transitionObj, epicenter)
    }

    override fun addTargets(transitionObj: Any, views: ArrayList<View>) {
        delegate.addTargets(transitionObj, views)
    }

    override fun mergeTransitionsTogether(transition1: Any?, transition2: Any?, transition3: Any?): Any {
        return delegate.mergeTransitionsTogether(transition1, transition2, transition3)
    }

    override fun scheduleHideFragmentView(exitTransitionObj: Any, fragmentView: View, exitingViews: ArrayList<View>) {
        return delegate.scheduleHideFragmentView(exitTransitionObj, fragmentView, exitingViews)
    }

    override fun mergeTransitionsInSequence(exitTransitionObj: Any?, enterTransitionObj: Any?, sharedElementTransitionObj: Any?): Any {
        return delegate.mergeTransitionsInSequence(exitTransitionObj, enterTransitionObj, sharedElementTransitionObj)
    }

    override fun beginDelayedTransition(sceneRoot: ViewGroup, transition: Any?) {
        // 优化过渡动画的捕获流程，达到仅捕获target的效果
        FragmentTransitionCompat21Set.addTargets(transition)
        return delegate.beginDelayedTransition(sceneRoot, transition)
    }

    override fun scheduleRemoveTargets(
        overallTransitionObj: Any,
        enterTransition: Any?, enteringViews: ArrayList<View>?,
        exitTransition: Any?, exitingViews: ArrayList<View>?,
        sharedElementTransition: Any?, sharedElementsIn: ArrayList<View>?
    ) {
        delegate.scheduleRemoveTargets(
            overallTransitionObj,
            enterTransition, enteringViews,
            exitTransition, exitingViews,
            sharedElementTransition, sharedElementsIn
        )
    }

    override fun setListenerForTransitionEnd(
        outFragment: Fragment, transition: Any,
        signal: CancellationSignal, transitionCompleteRunnable: Runnable
    ) {
        delegate.setListenerForTransitionEnd(outFragment, transition, signal, transitionCompleteRunnable)
    }

    override fun swapSharedElementTargets(
        sharedElementTransitionObj: Any?,
        sharedElementsOut: ArrayList<View>?,
        sharedElementsIn: ArrayList<View>?
    ) {
        delegate.swapSharedElementTargets(sharedElementTransitionObj, sharedElementsOut, sharedElementsIn)
    }

    override fun replaceTargets(transitionObj: Any, oldTargets: ArrayList<View>?, newTargets: ArrayList<View>?) {
        delegate.replaceTargets(transitionObj, oldTargets, newTargets)
    }

    override fun addTarget(transitionObj: Any, view: View) {
        delegate.addTarget(transitionObj, view)
    }

    override fun removeTarget(transitionObj: Any, view: View) {
        delegate.removeTarget(transitionObj, view)
    }

    companion object {
        private var ref: WeakReference<Any>? = null

        @CheckResult
        fun merge(transition: Transition): Transition {
            installIfNecessary()
            if (ref?.get() !is FragmentTransitionCompat21Enhance) return transition
            return FragmentTransitionCompat21Set(transition)
        }

        private fun installIfNecessary() {
            assert(Thread.currentThread() === Looper.getMainLooper().thread)
            if (ref?.get() != null) return
            val field = runCatching {
                val clazz = Class.forName("androidx.fragment.app.FragmentTransition")
                clazz.getDeclaredField("PLATFORM_IMPL")
            }.getOrNull()?.apply { isAccessible = true }

            val impl = field?.get(null) as? FragmentTransitionImpl
            ref = when (impl) {
                null -> WeakReference(this)
                is FragmentTransitionCompat21Enhance -> WeakReference(impl)
                else -> {
                    val compat = FragmentTransitionCompat21Enhance(impl)
                    field.set(null, compat)
                    WeakReference(compat)
                }
            }
        }
    }
}

private class FragmentTransitionCompat21Set constructor() : TransitionSet() {

    constructor(child: Transition) : this() {
        addTransition(child)
    }

    fun getChildTargets(targets: MutableSet<View>) {
        for (i in 0 until transitionCount) {
            getChildTargets(getTransitionAt(i), targets)
        }
    }

    fun getChildTargetIds(targetIds: MutableSet<Int>) {
        for (i in 0 until transitionCount) {
            getChildTargetIds(getTransitionAt(i), targetIds)
        }
    }

    private fun getChildTargets(child: Transition, targets: MutableSet<View>) {
        if (child.targetNames.isNullOrEmpty() && child.targetTypes.isNullOrEmpty()) {
            val childTargets = child.targets
            for (i in childTargets.indices) targets.add(childTargets[i])
        }
        if (child is TransitionSet) {
            val transitionCount = child.transitionCount
            for (i in 0 until transitionCount) {
                getChildTargets(child.getTransitionAt(i), targets)
            }
        }
    }

    private fun getChildTargetIds(child: Transition, targetIds: MutableSet<Int>) {
        if (child.targetNames.isNullOrEmpty() && child.targetTypes.isNullOrEmpty()) {
            val childTargetIds = child.targetIds
            for (i in childTargetIds.indices) targetIds.add(childTargetIds[i])
        }
        if (child is TransitionSet) {
            val transitionCount = child.transitionCount
            for (i in 0 until transitionCount) {
                getChildTargetIds(child.getTransitionAt(i), targetIds)
            }
        }
    }

    companion object {

        fun addTargets(transition: Any?) {
            if (transition !is Transition) return
            val targets = mutableSetOf<View>()
            val targetIds = mutableSetOf<Int>()
            val skip = transition !is FragmentTransitionCompat21Set
            recursionSet(transition, targets, targetIds, skip)
            targets.forEach(transition::addTarget)
            targetIds.forEach(transition::addTarget)
            // 调整匹配顺序，支持捕获的values.view不同，但targetId相同的情况
            transition.setMatchOrder(MATCH_NAME, MATCH_ID, MATCH_INSTANCE, MATCH_ITEM_ID)
        }

        private fun recursionSet(
            transition: Transition,
            targets: MutableSet<View>,
            targetIds: MutableSet<Int>,
            skip: Boolean
        ) {
            if (transition is FragmentTransitionCompat21Set && !skip) {
                transition.getChildTargets(targets)
                transition.getChildTargetIds(targetIds)
            }
            if (transition is TransitionSet) {
                val transitionCount = transition.transitionCount
                for (i in 0 until transitionCount) {
                    recursionSet(transition.getTransitionAt(i), targets, targetIds, skip = false)
                }
            }
        }
    }
}