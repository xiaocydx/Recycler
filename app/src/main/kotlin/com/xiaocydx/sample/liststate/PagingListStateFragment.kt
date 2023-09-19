package com.xiaocydx.sample.liststate

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.xiaocydx.cxrv.divider.Edge
import com.xiaocydx.cxrv.divider.divider
import com.xiaocydx.cxrv.itemclick.doOnSimpleItemClick
import com.xiaocydx.cxrv.list.ListAdapter
import com.xiaocydx.cxrv.list.ListOwner
import com.xiaocydx.cxrv.list.ListState
import com.xiaocydx.cxrv.list.adapter
import com.xiaocydx.cxrv.list.fixedSize
import com.xiaocydx.cxrv.list.grid
import com.xiaocydx.cxrv.list.linear
import com.xiaocydx.cxrv.list.removeItem
import com.xiaocydx.cxrv.paging.onEach
import com.xiaocydx.cxrv.paging.pagingCollector
import com.xiaocydx.cxrv.paging.storeIn
import com.xiaocydx.sample.R
import com.xiaocydx.sample.databinding.FragmentListStateBinding
import com.xiaocydx.sample.dp
import com.xiaocydx.sample.foo.FooListAdapter
import com.xiaocydx.sample.launchRepeatOnLifecycle
import com.xiaocydx.sample.paging.config.replaceWithSwipeRefresh
import com.xiaocydx.sample.paging.config.withPaging
import com.xiaocydx.sample.viewLifecycle
import com.xiaocydx.sample.viewLifecycleScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * [ListState]和[ListAdapter]建立基于[ListOwner]的双向通信，
 * [storeIn]转换的结果可以同时被多个收集器收集，共享列表状态。
 *
 * **注意**：虽然支持[ListState]和[ListAdapter]之间的双向通信，
 * 但是建议以单向数据流的方式更新列表，即仅通过[ListState]更新列表，
 * 这会提高代码的可读性和可维护性。
 *
 * ### 为什么不用[LiveData]或者[StateFlow]？
 * [LiveData]和[StateFlow]只能替换列表，让视图控制器执行一次差异计算，不支持细粒度的更新操作。
 * [ListState]支持细粒度的更新操作，例如在视图控制器处于活跃状态时，调用[ListState.addItem]，
 * 只需要将更新操作以事件的形式发送到视图控制器即可，不需要执行一次差异计算。
 *
 * @author xcc
 * @date 2023/8/17
 */
class PagingListStateFragment : Fragment(R.layout.fragment_list_state) {
    private val sharedViewModel: ListStateSharedViewModel by activityViewModels()
    private val pagingViewModel: PagingListStateViewModel by viewModels()
    private val fooAdapter1 = FooListAdapter()
    private val fooAdapter2 = FooListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FragmentListStateBinding.bind(view).initView().initCollect()
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentListStateBinding.initView() = apply {
        tvFoo1.text = "PagingList-1"
        fooAdapter1.doOnSimpleItemClick(fooAdapter1::removeItem)
        rvFoo1.linear().fixedSize().adapter(fooAdapter1.withPaging())
            .divider(5.dp, 5.dp) { edge(Edge.all()).color(0xFF979EC4.toInt()) }
            .replaceWithSwipeRefresh(fooAdapter1)

        tvFoo2.text = "PagingList-2"
        fooAdapter2.doOnSimpleItemClick(fooAdapter2::removeItem)
        rvFoo2.grid(spanCount = 2).fixedSize().adapter(fooAdapter2.withPaging())
            .divider(5.dp, 5.dp) { edge(Edge.all()).color(0xFF979EC4.toInt()) }
            .replaceWithSwipeRefresh(fooAdapter2)
    }

    private fun FragmentListStateBinding.initCollect() = apply {
        pagingViewModel.flow
            .onEach(fooAdapter1.pagingCollector)
            .launchRepeatOnLifecycle(viewLifecycle)

        pagingViewModel.flow
            .onEach(fooAdapter2.pagingCollector)
            .launchRepeatOnLifecycle(viewLifecycle)

        sharedViewModel.menuAction.onEach { action ->
            when (action) {
                MenuAction.REFRESH -> pagingViewModel.refresh()
                MenuAction.LIST_STATE_INSERT_ITEM -> pagingViewModel.insertItem()
                MenuAction.LIST_STATE_REMOVE_ITEM -> pagingViewModel.deleteItem()
                MenuAction.CLEAR_ODD_ITEM -> pagingViewModel.clearOddItem()
                MenuAction.CLEAR_EVEN_ITEM -> pagingViewModel.clearEvenItem()
                MenuAction.CLEAR_ALL_ITEM -> pagingViewModel.clearAllItem()
                else -> return@onEach
            }
        }.launchIn(viewLifecycleScope)
    }
}