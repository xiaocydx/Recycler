package com.xiaocydx.recycler.paging

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import com.xiaocydx.recycler.TestActivity
import com.xiaocydx.recycler.extension.withHeader
import com.xiaocydx.recycler.list.ListAdapter
import com.xiaocydx.recycler.list.insertItem
import com.google.common.truth.Truth.assertThat
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [LoadFooterAdapter]的单元测试
 *
 * @author xcc
 * @date 2021/12/11
 */
@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(RobolectricTestRunner::class)
class LoadFooterAdapterTest {
    private val adapter = TestListAdapter()
    private val collector = adapter.pagingCollector
    private val onCreate: OnCreateLoadView<View> = spyk({ View(it.context) })
    private lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario
            .launch(TestActivity::class.java)
            .moveToState(Lifecycle.State.CREATED)
            .onActivity { activity ->
                activity.recyclerView.layoutManager = LinearLayoutManager(activity)
            }
    }

    @Test
    fun createAndShow_LoadingView() {
        val onBind: OnBindLoadView<View> = spyk({
            assertThat(it.isShown).isTrue()
            assertThat(it.visibility).isEqualTo(View.VISIBLE)
        })
        val concatAdapter = getConcatAdapter {
            loading<View> {
                onCreateView(onCreate)
                onBindView(onBind)
            }
        }
        scenario.onActivity { activity ->
            activity.recyclerView.adapter = concatAdapter
            adapter.insertItem("A")
            collector.setLoadState(LoadType.APPEND, LoadState.Loading)
            verify(exactly = 1) { onCreate(collector, ofType()) }
            verify(exactly = 1) { onBind(collector, ofType()) }
        }
    }

    @Test
    fun createAndShow_FullyView() {
        val onBind: OnBindLoadView<View> = spyk({
            assertThat(it.isShown).isTrue()
            assertThat(it.visibility).isEqualTo(View.VISIBLE)
        })
        val concatAdapter = getConcatAdapter {
            fully<View> {
                onCreateView(onCreate)
                onBindView(onBind)
            }
        }
        scenario.onActivity { activity ->
            activity.recyclerView.adapter = concatAdapter
            adapter.insertItem("A")
            collector.setLoadState(LoadType.APPEND, LoadState.Success(isFully = true))
            verify(exactly = 1) { onCreate(collector, ofType()) }
            verify(exactly = 1) { onBind(collector, ofType()) }
        }
    }

    @Test
    fun createAndShow_FailureView() {
        val onBind: OnBindLoadViewWithException<View> = spyk({ view, _ ->
            assertThat(view.isShown).isTrue()
            assertThat(view.visibility).isEqualTo(View.VISIBLE)
        })
        val concatAdapter = getConcatAdapter {
            failure<View> {
                onCreateView(onCreate)
                onBindView(onBind)
            }
        }
        scenario.onActivity { activity ->
            activity.recyclerView.adapter = concatAdapter
            adapter.insertItem("A")
            val exception = IllegalArgumentException()
            collector.setLoadState(LoadType.APPEND, LoadState.Failure(exception))
            verify(exactly = 1) { onCreate(collector, ofType()) }
            verify(exactly = 1) { onBind(collector, ofType(), exception) }
        }
    }

    @Test
    fun insertItem_CreateAndShow_FullyView() {
        val onBind: OnBindLoadView<View> = spyk({
            assertThat(it.isShown).isTrue()
            assertThat(it.visibility).isEqualTo(View.VISIBLE)
        })
        val concatAdapter = getConcatAdapter {
            fully<View> {
                onCreateView(onCreate)
                onBindView(onBind)
            }
        }
        scenario.onActivity { activity ->
            activity.recyclerView.adapter = concatAdapter
            collector.setLoadState(LoadType.APPEND, LoadState.Success(isFully = true))
            verify(exactly = 0) { onCreate(collector, ofType()) }
            verify(exactly = 0) { onBind(collector, ofType()) }

            adapter.insertItem("A")
            verify(exactly = 1) { onCreate(collector, ofType()) }
            verify(exactly = 1) { onBind(collector, ofType()) }
        }
    }

    private inline fun getConcatAdapter(
        block: LoadFooter.Config.() -> Unit
    ): ConcatAdapter {
        val config = LoadFooter.Config().also(block)
        return adapter.withHeader(LoadFooterAdapter(config, adapter))
    }

    private class TestListAdapter : ListAdapter<String, ViewHolder>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            error("areItemsTheSame")
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return object : ViewHolder(View(parent.context)) {}
        }
    }
}