package com.xiaocydx.sample.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xiaocydx.cxrv.list.*
import com.xiaocydx.cxrv.paging.pagingCollector
import com.xiaocydx.sample.*
import com.xiaocydx.sample.paging.MenuAction.*
import com.xiaocydx.sample.viewmodel.activityViewModels
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author xcc
 * @date 2022/2/17
 */
abstract class PagingFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    protected val viewModel: PagingViewModel by activityViewModels(
        key = { this::class.java.simpleName },
        factoryProducer = { PagingViewModel.Factory }
    )
    protected val adapter = FooAdapter()
    protected lateinit var rvPaging: RecyclerView
        private set

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FrameLayout(requireContext()).apply {
        rvPaging = RecyclerView(requireContext()).apply {
            id = viewModel.rvId
            overScrollMode = OVER_SCROLL_NEVER
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        addView(rvPaging)
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initCollect()
        initEdgeToEdge()
    }

    protected abstract fun initView()

    @CallSuper
    protected open fun initCollect() {
        sharedViewModel.menuAction.onEach { action ->
            when (action) {
                INCREASE_SPAN_COUNT -> increaseSpanCount()
                DECREASE_SPAN_COUNT -> decreaseSpanCount()
                REVERSE_LAYOUT -> reverseLayout()
                REFRESH -> refresh()
                ADAPTER_INSERT_ITEM -> adapterInsertItem()
                ADAPTER_DELETE_ITEM -> adapterDeleteItem()
                LIST_STATE_INSERT_ITEM -> listStateInsertItem()
                LIST_STATE_DELETE_ITEM -> listStateDeleteItem()
                CLEAR_ODD_ITEM -> clearOddItem()
                CLEAR_EVEN_ITEM -> clearEvenItem()
                CLEAR_ALL_ITEM -> clearAllItem()
                else -> return@onEach
            }
        }.launchIn(viewLifecycleScope)
    }

    private fun initEdgeToEdge() {
        rvPaging.clipToPadding = false
        rvPaging.layoutManager?.enableViewBoundCheckCompat()
        rvPaging.doOnApplyWindowInsetsCompat { view, insets, initialState ->
            view.updatePadding(bottom = insets.getNavigationBarHeight() + initialState.paddings.bottom)
        }
    }

    private fun increaseSpanCount() {
        when (val lm: LayoutManager? = rvPaging.layoutManager) {
            is GridLayoutManager -> lm.spanCount += 1
            is StaggeredGridLayoutManager -> lm.spanCount += 1
            else -> return
        }
        rvPaging.invalidateItemDecorations()
    }

    private fun decreaseSpanCount() {
        when (val lm: LayoutManager? = rvPaging.layoutManager) {
            is GridLayoutManager -> if (lm.spanCount > 1) lm.spanCount -= 1
            is StaggeredGridLayoutManager -> if (lm.spanCount > 1) lm.spanCount -= 1
            else -> return
        }
        rvPaging.invalidateItemDecorations()
    }

    private fun reverseLayout() {
        when (val lm: LayoutManager? = rvPaging.layoutManager) {
            is LinearLayoutManager -> lm.reverseLayout = !lm.reverseLayout
            is StaggeredGridLayoutManager -> lm.reverseLayout = !lm.reverseLayout
            else -> return
        }
        rvPaging.invalidateItemDecorations()
    }

    private fun refresh() {
        // viewModel.refresh()
        adapter.pagingCollector.refresh()
    }

    private fun adapterInsertItem() {
        val item = viewModel.createFoo(
            tag = "Adapter",
            num = adapter.currentList.size
        )
        adapter.addItem(0, item)
    }

    private fun adapterDeleteItem() {
        adapter.removeItemAt(0)
    }

    private fun listStateInsertItem() {
        viewModel.insertItem()
    }

    private fun listStateDeleteItem() {
        viewModel.deleteItem()
    }

    private fun clearOddItem() {
        adapter.submitTransform {
            filter { it.num % 2 == 0 }
        }
    }

    private fun clearEvenItem() {
        adapter.submitTransform {
            filter { it.num % 2 != 0 }
        }
    }

    private fun clearAllItem() {
        adapter.clear()
    }
}