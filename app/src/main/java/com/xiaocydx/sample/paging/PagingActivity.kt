package com.xiaocydx.sample.paging

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.xiaocydx.recycler.binding.bindingAdapter
import com.xiaocydx.recycler.extension.adapter
import com.xiaocydx.recycler.extension.divider
import com.xiaocydx.recycler.extension.doOnSimpleItemClick
import com.xiaocydx.recycler.extension.linear
import com.xiaocydx.recycler.list.submitList
import com.xiaocydx.sample.R
import com.xiaocydx.sample.databinding.ActivityPagingBinding
import com.xiaocydx.sample.databinding.ItemMenuBinding
import com.xiaocydx.sample.dp
import com.xiaocydx.sample.paging.MenuAction.*
import com.xiaocydx.sample.showToast

/**
 * 分页加载示例代码（本地测试）
 *
 * 页面配置发生变更时（例如旋转屏幕），保留分页加载数据、列表滚动位置。
 *
 * @author xcc
 * @date 2022/2/17
 */
class PagingActivity : AppCompatActivity() {
    private val viewModel: SharedViewModel by viewModels()
    private lateinit var binding: ActivityPagingBinding
    private val fragmentTag = PagingFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initMenuDrawer()
        initPagingFragment()
    }

    private fun initMenuDrawer() {
        binding.rvMenu
            .linear()
            .divider {
                height = 0.5.dp
                color = 0xFFD5D5D5.toInt()
            }.adapter(bindingAdapter(
                uniqueId = MenuAction::text,
                inflate = ItemMenuBinding::inflate
            ) {
                onBindView { root.text = it.text }
                doOnSimpleItemClick(::executeMenuAction)
                submitList(values().toList())
            })
    }

    private fun executeMenuAction(action: MenuAction) {
        when (action) {
            LINEAR_LAYOUT -> initLinearLayout()
            GIRD_LAYOUT -> initGridLayout()
            STAGGERED_LAYOUT -> initStaggeredLayout()
            else -> viewModel.submitMenuAction(action)
        }
        binding.root.closeDrawer(binding.rvMenu)
        showToast(action.text)
    }

    private fun initPagingFragment() {
        if (supportFragmentManager.findFragmentByTag(fragmentTag) == null) {
            initLinearLayout()
        }
    }

    private fun initLinearLayout() {
        replaceFragment(LinearLayoutFragment())
    }

    private fun initGridLayout() {
        replaceFragment(GridLayoutFragment())
    }

    private fun initStaggeredLayout() {
        replaceFragment(StaggeredLayoutFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.flContainer, fragment, fragmentTag)
            .commit()
        supportActionBar?.title = fragment.javaClass.simpleName
    }
}