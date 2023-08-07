package com.xiaocydx.sample.paging.complex

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import com.xiaocydx.sample.doOnStateChanged
import com.xiaocydx.sample.enableGestureNavBarEdgeToEdge
import com.xiaocydx.sample.paging.complex.transform.SystemBarsContainer
import com.xiaocydx.sample.paging.complex.transform.TransformReceiver
import com.xiaocydx.sample.paging.complex.transform.setLightStatusBarOnResume
import com.xiaocydx.sample.paging.complex.transform.setWindowNavigationBarColor
import com.xiaocydx.sample.transition.EnterTransitionController
import com.xiaocydx.sample.transition.LOADING_DURATION
import com.xiaocydx.sample.transition.SlideState
import com.xiaocydx.sample.transition.TransitionFragment
import com.xiaocydx.sample.viewLifecycle
import com.xiaocydx.sample.viewLifecycleScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * [EnterTransitionController]推迟Fragment过渡动画，推迟时间未到达，列表数据加载完成，申请重新布局，
 * 并开始Fragment过渡动画，此时的交互体验接近Activity的窗口动画，即看到Fragment页面就有列表内容，
 * 而不是先显示Loading，再看到列表内容。
 *
 * 对于列表数据加载比较快的情况，这种处理方式虽然没直接解决重新布局的耗时问题，但是能提升交互体验。
 *
 * @author xcc
 * @date 2023/8/4
 */
class AdFragment : TransitionFragment(), TransformReceiver {
    private val complexViewModel: ComplexListViewModel by viewModels(
        ownerProducer = { parentFragment ?: requireActivity() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SystemBarsContainer(requireContext())
        .setLightStatusBarOnResume(this)
        .setStatusBarColor(0xFF79AA91.toInt())
        .setWindowNavigationBarColor(this)
        .setGestureNavBarEdgeToEdge(true)
        .attach(super.onCreateView(inflater, container, savedInstanceState))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycle.doOnStateChanged { source, event ->
            val currentState = source.lifecycle.currentState
            Log.d("AdFragment", "currentState = ${currentState}, event = $event")
        }

        arguments?.getString(KET_ID)
            .takeIf { savedInstanceState != null }
            ?.let(complexViewModel::syncSelectId)
        setTransformEnterTransition().duration = 200
        recyclerView.enableGestureNavBarEdgeToEdge()

        // 沿用EnterTransitionController解决过渡动画卡顿的问题
        val controller = EnterTransitionController(this)
        controller.postponeEnterTransition(timeMillis = LOADING_DURATION + 50L)
        viewModel.state
            .flowWithLifecycle(viewLifecycle)
            .distinctUntilChanged()
            .onEach { state ->
                when (state) {
                    SlideState.LOADING -> {
                        loadingAdapter.showLoading()
                    }
                    SlideState.CONTENT -> {
                        controller.startPostponeEnterTransitionOrAwait()
                        loadingAdapter.hideLoading()
                        contentAdapter.insertItems()
                    }
                }
            }
            .launchIn(viewLifecycleScope)
    }

    companion object {
        private const val KET_ID = "KEY_ID"

        fun createArgs(currentId: String) = Bundle(1)
            .apply { putString(KET_ID, currentId) }
    }
}