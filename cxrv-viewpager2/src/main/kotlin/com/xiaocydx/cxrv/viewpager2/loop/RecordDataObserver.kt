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

package com.xiaocydx.cxrv.viewpager2.loop

import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

/**
 * 在每次更新时，记录`adapter.itemCount`为[lastItemCount]
 *
 * @author xcc
 * @date 2023/5/11
 */
internal class RecordDataObserver(private val adapter: Adapter<*>) : AdapterDataObserver() {
    var lastItemCount = adapter.itemCount
        private set

    private fun recordItemCount() {
        lastItemCount = adapter.itemCount
    }

    override fun onChanged() = recordItemCount()
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = onChanged()
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) = onChanged()
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onChanged()
    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onChanged()
    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = onChanged()
}