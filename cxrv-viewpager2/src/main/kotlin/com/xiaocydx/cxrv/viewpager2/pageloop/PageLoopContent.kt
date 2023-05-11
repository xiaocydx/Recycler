/*
 * Copyright 2022 xiaocydx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaocydx.cxrv.viewpager2.pageloop

import androidx.recyclerview.widget.PageLoopAdapter
import androidx.recyclerview.widget.PageLoopCallback
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewpager2.widget.ViewPager2

/**
 * @author xcc
 * @date 2023/5/11
 */
internal class PageLoopContent(val adapter: Adapter<*>, val extraPageLimit: Int) {

    val itemCount: Int
        get() = adapter.itemCount

    val canLoop: Boolean
        get() = itemCount > 1

    fun createPageLoopCallback(viewPager2: ViewPager2) = PageLoopCallback(this, viewPager2)

    fun createPageLoopAdapter(updateAnchor: () -> Unit) = PageLoopAdapter(this, updateAnchor)

    fun toBindingAdapterPosition(layoutPosition: Int): Int {
        if (!canLoop) return layoutPosition
        var bindingAdapterPosition = (layoutPosition - extraPageLimit) % itemCount
        if (bindingAdapterPosition < 0) {
            bindingAdapterPosition += itemCount
        }
        return bindingAdapterPosition
    }
}