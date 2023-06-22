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

package com.xiaocydx.cxrv.multitype

import com.xiaocydx.cxrv.list.ListAdapter

/**
 * 多类型作用域实现类
 *
 * @author xcc
 * @date 2022/4/16
 */
@PublishedApi
internal class MultiTypeScopeImpl<T : Any> : MultiTypeScope<T>() {
    private val multiType = MutableMultiTypeImpl<T>()
    private val multiTypeAdapter = MultiTypeAdapter<T>()
    override val listAdapter: ListAdapter<T, *>
        get() = multiTypeAdapter

    override val size: Int
        get() = multiType.size

    override fun keyAt(viewType: Int) = multiType.keyAt(viewType)

    override fun valueAt(index: Int) = multiType.valueAt(index)

    override fun itemAt(item: T) = multiType.itemAt(item)

    override fun register(type: Type<out T>) = multiType.register(type)

    fun complete(): ListAdapter<T, *> {
        multiType.complete()
        multiTypeAdapter.setMultiType(multiType)
        return multiTypeAdapter
    }
}