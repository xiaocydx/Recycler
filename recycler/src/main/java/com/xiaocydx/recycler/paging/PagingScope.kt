package com.xiaocydx.recycler.paging

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.xiaocydx.recycler.extension.withFooter
import com.xiaocydx.recycler.extension.withHeader
import com.xiaocydx.recycler.list.ListAdapter
import com.xiaocydx.recycler.marker.RvDslMarker
import com.xiaocydx.recycler.widget.ViewAdapter

/**
 * 分页初始化作用域
 *
 * @author xcc
 * @date 2022/3/8
 */
@RvDslMarker
open class PagingScope {
    private var needHeader = true
    private var needFooter = true
    private var loadHeader: ViewAdapter<*>? = null
    private var loadFooter: ViewAdapter<*>? = null
    private var initHeader: (LoadHeader.Config.() -> Unit)? = null
    private var initFooter: (LoadFooter.Config.() -> Unit)? = null

    /**
     * 是否启用item动画
     */
    var enabledItemAnim: Boolean = true

    /**
     * 内容适配器
     */
    var listAdapter: ListAdapter<*, *>? = null

    /**
     * 设置加载头部适配器
     *
     * @param adapter 传入`null`表示不需要加载头部
     */
    fun loadHeader(adapter: ViewAdapter<*>?) {
        loadHeader = adapter
        initHeader = null
        needHeader = adapter != null
    }

    /**
     * 设置加载头部配置
     *
     * 详细的加载头部配置描述[LoadHeader.Config]
     * ```
     * loadHeader {
     *     loadingView { parent -> ProgressBar(parent.context) }
     *     emptyView { parent -> TextView(parent.context) }
     *     failureView { parent -> TextView(parent.context) }
     *
     *     // 在显示视图时执行某些操作，可以用以下写法
     *     loading<ProgressBar> {
     *         onCreateView { parent -> ProgressBar(parent.context) }
     *         onBindView { view -> ... }
     *     }
     *     empty<TextView> {
     *         onCreateView { parent -> TextView(parent.context) }
     *         onBindView { view -> view.text = "empty" }
     *     }
     *     failure<TextView> {
     *         onCreateView { parent -> TextView(parent.context) }
     *         onBindView { view, exception -> view.text = "failure" }
     *     }
     * }
     * ```
     */
    fun loadHeader(block: LoadHeader.Config.() -> Unit) {
        loadHeader = null
        initHeader = block
        needHeader = true
    }

    /**
     * 设置加载尾部适配器
     *
     * @param adapter 传入`null`表示不需要加载尾部
     */
    fun loadFooter(adapter: ViewAdapter<*>?) {
        loadFooter = adapter
        initFooter = null
        needFooter = adapter != null
    }

    /**
     * 设置加载尾部配置
     *
     * 详细的加载尾部配置描述[LoadFooter.Config]
     * ```
     * loadFooter {
     *     loadingView { parent -> ProgressBar(parent.context) }
     *     fullyView { parent -> TextView(parent.context) }
     *     failureView { parent -> TextView(parent.context) }
     *
     *     // 在显示视图时执行某些操作，可以用以下写法
     *     loading<ProgressBar> {
     *         onCreateView { parent -> ProgressBar(parent.context) }
     *         onBindView { view -> ... }
     *     }
     *     fully<TextView> {
     *         onCreateView { parent -> TextView(parent.context) }
     *         onBindView { view -> view.text = "fully" }
     *     }
     *     failure<TextView> {
     *         onCreateView { parent -> TextView(parent.context) }
     *         onBindView { view, exception -> view.text = "failure" }
     *     }
     * }
     * ```
     */
    fun loadFooter(block: LoadFooter.Config.() -> Unit) {
        loadFooter = null
        initFooter = block
        needFooter = true
    }

    protected open fun init(rv: RecyclerView) {
        val sourceAdapter: Adapter<*>? = rv.adapter
        if (sourceAdapter is ConcatAdapter) {
            sourceAdapter.apply {
                addAdapterIfNonNull(getFinalLoadFooter())
                addAdapter(getFinalListAdapter())
                addAdapterIfNonNull(getFinalLoadFooter())
            }
        } else {
            rv.adapter = getFinalListAdapter()
                .withHeaderIfNonNull(getFinalLoadHeader())
                .withFooterIfNonNull(getFinalLoadFooter())
        }
        if (!enabledItemAnim) {
            rv.itemAnimator = null
        }
        clear()
    }

    /**
     * 加载头部默认配置，返回`true`表示有默认配置
     */
    protected open fun LoadHeader.Config.withDefault(): Boolean = false

    /**
     * 加载尾部默认配置，返回`true`表示有默认配置
     */
    protected open fun LoadFooter.Config.withDefault(): Boolean = false

    protected fun getFinalListAdapter(): ListAdapter<*, *> = requireNotNull(listAdapter) {
        "未设置或已清除ListAdapter<*, *>，无法连接LoadHeader、ListAdapter<*, *>、LoadFooter。"
    }

    private fun getFinalLoadHeader(): ViewAdapter<*>? {
        if (!needHeader) {
            return null
        }
        loadHeader?.let { return it }
        val config = LoadHeader.Config()
        val hasDefault = config.withDefault()
        if (!hasDefault && initHeader == null) {
            return null
        }
        initHeader?.invoke(config)
        return LoadHeaderAdapter(config, getFinalListAdapter())
    }

    private fun getFinalLoadFooter(): ViewAdapter<*>? {
        if (!needFooter) {
            return null
        }
        loadFooter?.let { return it }
        val config = LoadFooter.Config()
        val hasDefault = config.withDefault()
        if (!hasDefault && initFooter == null) {
            return null
        }
        initFooter?.invoke(config)
        return LoadFooterAdapter(config, getFinalListAdapter())
    }

    private fun ConcatAdapter.addAdapterIfNonNull(adapter: ViewAdapter<*>?) {
        adapter?.let(::addAdapter)
    }

    private fun Adapter<*>.withHeaderIfNonNull(adapter: ViewAdapter<*>?): Adapter<*> {
        return if (adapter != null) withHeader(adapter) else this
    }

    private fun Adapter<*>.withFooterIfNonNull(adapter: ViewAdapter<*>?): Adapter<*> {
        return if (adapter != null) withFooter(adapter) else this
    }

    private fun clear() {
        loadHeader = null
        loadFooter = null
        initFooter = null
        initHeader = null
        listAdapter = null
    }
}