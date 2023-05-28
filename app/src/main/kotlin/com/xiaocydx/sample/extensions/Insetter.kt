@file:Suppress("PackageDirectoryMismatch")

package com.xiaocydx.sample

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.CheckResult
import androidx.annotation.Px
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.core.view.WindowInsetsCompat.Type.*
import androidx.recyclerview.widget.RecyclerView
import com.xiaocydx.cxrv.list.enableBoundCheckCompat
import java.lang.ref.WeakReference

/**
 * 启用手势导航栏边到边的示例代码
 */
fun Window.enableGestureNavBarEdgeToEdge() {
    val contentRootRef = (decorView as ViewGroup).children
        .firstOrNull { it is ViewGroup }?.let(::WeakReference)
    WindowCompat.setDecorFitsSystemWindows(this, false)
    ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
        var decorInsets = insets
        if (decorInsets.isGestureNavigationBar(v.resources)) {
            decorInsets = decorInsets.consume(navigationBars())
        }
        ViewCompat.onApplyWindowInsets(v, decorInsets)
        contentRootRef?.get()?.updateMargins(
            top = decorInsets.getInsets(statusBars()).top,
            bottom = decorInsets.getInsets(navigationBars()).bottom
        )
        insets
    }
    decorView.doOnAttach(ViewCompat::requestApplyInsets)
}

fun RecyclerView.enableGestureNavBarEdgeToEdge() {
    clipToPadding = false
    layoutManager?.enableBoundCheckCompat()
    doOnApplyWindowInsets { v, insets, initialState ->
        v.updatePadding(bottom = when {
            insets.isGestureNavigationBar(v.resources) -> {
                val navigationBars = insets.getInsets(navigationBars())
                navigationBars.bottom + initialState.paddings.bottom
            }
            else -> initialState.paddings.bottom
        })
    }
}

fun View.doOnApplyWindowInsets(
    block: (view: View, insets: WindowInsetsCompat, initialState: ViewState) -> Unit
) {
    val initialState = ViewState(this)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        block(view, insets, initialState)
        insets
    }
    doOnAttach(ViewCompat::requestApplyInsets)
}

private fun View.updateMargins(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val changed = left != marginLeft || top != marginTop
            || right != marginTop || bottom != marginBottom
    params.setMargins(left, top, right, bottom)
    if (changed) layoutParams = params
}

/**
 * 消费指定[InsetsType]类型集的`Insets`
 *
 * ```
 * val typeMask = WindowInsetsCompat.Type.statusBars()
 * val outcome = insets.consume(typeMask)
 * outcome.getInsets(typeMask) // Insets.NONE
 * outcome.getInsetsIgnoringVisibility() // Insets.NONE
 * outcome.isVisible(typeMask) // 不改变可见结果
 * ```
 */
@CheckResult
fun WindowInsetsCompat.consume(@InsetsType typeMask: Int): WindowInsetsCompat {
    if (typeMask <= 0) return this
    val builder = WindowInsetsCompat.Builder(this)
    if (typeMask != ime()) {
        // typeMask等于IME会抛出IllegalArgumentException
        builder.setInsetsIgnoringVisibility(typeMask, Insets.NONE)
    }
    return builder.setInsets(typeMask, Insets.NONE).build()
}

private fun WindowInsetsCompat.isGestureNavigationBar(resources: Resources): Boolean {
    val threshold = (24 * resources.displayMetrics.density).toInt()
    return getInsets(navigationBars()).bottom <= threshold.coerceAtLeast(66)
}

/**
 * 视图的初始状态
 */
data class ViewState internal constructor(
    /**
     * 视图的初始params
     */
    val params: ViewParams,

    /**
     * 视图的初始paddings
     */
    val paddings: ViewPaddings,
) {
    internal constructor(view: View) : this(ViewParams(view), ViewPaddings(view))
}

/**
 * 视图的初始params
 */
data class ViewParams internal constructor(
    @Px val width: Int,
    @Px val height: Int,
    @Px val marginLeft: Int,
    @Px val marginTop: Int,
    @Px val marginRight: Int,
    @Px val marginBottom: Int
) {
    internal constructor(view: View) : this(
        width = view.layoutParams?.width ?: 0,
        height = view.layoutParams?.height ?: 0,
        marginLeft = view.marginLeft,
        marginTop = view.marginTop,
        marginRight = view.marginRight,
        marginBottom = view.marginBottom
    )
}

/**
 * 视图的初始paddings
 */
data class ViewPaddings internal constructor(
    @Px val left: Int,
    @Px val top: Int,
    @Px val right: Int,
    @Px val bottom: Int
) {
    internal constructor(view: View) : this(
        left = view.paddingLeft,
        top = view.paddingTop,
        right = view.paddingRight,
        bottom = view.paddingBottom
    )
}