package com.wang.adapters.utils

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.wang.adapters.adapter.BaseAdapter
import com.wang.adapters.adapter.BaseExpandableAdapter
import com.wang.adapters.adapter.BaseListAdapter
import com.wang.adapters.adapter.BaseListCycleAdapter
import com.wang.adapters.adapter.BaseMultiItemAdapter
import com.wang.adapters.container.BaseContainerAdapter
import com.wang.adapters.container.bean.IContainerBean
import com.wang.adapters.holder.BaseViewHolder

inline fun createBaseAdapter(
    crossinline onViewTypeCallback: BaseAdapter.(position: Int) -> Int,
    crossinline onCountCallback: BaseAdapter.() -> Int,
    crossinline onCreateCallback: BaseAdapter.(parent: ViewGroup, viewType: Int) -> BaseViewHolder<ViewBinding>,
    crossinline onBindCallback: BaseAdapter.(holder: BaseViewHolder<*>, position: Int) -> Unit
) = object : BaseAdapter() {

    override fun getItemViewType(position: Int) = this.onViewTypeCallback(position)

    override fun getItemCount() = this.onCountCallback()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = this.onCreateCallback(parent, viewType)

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) = this.onBindCallback(holder, position)
}

/**
 * 不带ViewBinding功能，也不能使用[BaseViewHolder.vb]
 */
inline fun <B : Any> createListAdapter(
    list: List<B>? = null,
    crossinline onCreateCallback: BaseListAdapter<ViewBinding, B>.(parent: ViewGroup) -> BaseViewHolder<ViewBinding>,
    crossinline onBindCallback: BaseListAdapter<ViewBinding, B>.(holder: BaseViewHolder<*>, bean: B) -> Unit
) =
    object : BaseListAdapter<ViewBinding, B>(list) {
        override fun onBindListViewHolder(holder: BaseViewHolder<ViewBinding>, bean: B) =
            this.onBindCallback(holder, bean)

        override fun onCreateListViewHolder(parent: ViewGroup): BaseViewHolder<ViewBinding> =
            this.onCreateCallback(parent)
    }

/**
 * 在mvvm里，一般的adapter就是普通的v层，所以降级到activity里去
 */
inline fun <reified VB : ViewBinding, B : Any> createListVbAdapter(
    list: List<B>? = null,
    crossinline onBindCallback: BaseListAdapter<VB, B>.(holder: BaseViewHolder<VB>, bean: B) -> Unit
) =
    object : BaseListAdapter<VB, B>(list) {
        override fun onBindListViewHolder(holder: BaseViewHolder<VB>, bean: B) =
            this.onBindCallback(holder, bean)

        override fun onCreateListViewHolder(parent: ViewGroup): BaseViewHolder<VB> =
            BaseViewHolder(ViewBindingHelper.getViewBindingInstanceByClass<VB>(parent.layoutInflater, parent))
    }

/**
 * 带vb
 */
inline fun <reified VB : ViewBinding, B : Any> createListVbAdapter(
    list: List<B>? = null,
    crossinline onBindCallback: BaseListAdapter<VB, B>.(holder: BaseViewHolder<VB>, vb: VB, bean: B) -> Unit
) = createListVbAdapter(list) { holder, bean -> this.onBindCallback(holder, holder.vb, bean) }

inline fun <reified VB : ViewBinding, B : Any> createCycleAdapter(
    list: List<B>? = null,
    crossinline onBindCallback: BaseListCycleAdapter<VB, B>.(holder: BaseViewHolder<VB>, bean: B) -> Unit
) =
    object : BaseListCycleAdapter<VB, B>(list) {
        override fun onBindListViewHolder(holder: BaseViewHolder<VB>, bean: B) =
            this.onBindCallback(holder, bean)

        override fun onCreateListViewHolder(parent: ViewGroup): BaseViewHolder<VB> =
            BaseViewHolder(ViewBindingHelper.getViewBindingInstanceByClass<VB>(parent.layoutInflater, parent))
    }

/**
 * 带vb
 * @param onBindCallback holder的position见[BaseListCycleAdapter.cycleListPosition]
 */
inline fun <reified VB : ViewBinding, B : Any> createCycleAdapter(
    list: List<B>? = null,
    crossinline onBindCallback: BaseListCycleAdapter<VB, B>.(holder: BaseViewHolder<VB>, vb: VB, bean: B) -> Unit
) =
    object : BaseListCycleAdapter<VB, B>(list) {
        override fun onBindListViewHolder(holder: BaseViewHolder<VB>, bean: B) =
            this.onBindCallback(holder, holder.vb, bean)

        override fun onCreateListViewHolder(parent: ViewGroup): BaseViewHolder<VB> =
            BaseViewHolder(ViewBindingHelper.getViewBindingInstanceByClass<VB>(parent.layoutInflater, parent))
    }

inline fun <reified V1 : ViewBinding, reified V2 : ViewBinding, B : Any> createExpandableAdapter(
    crossinline childCountCallback: BaseExpandableAdapter<V1, V2, B>.(parentItem: B, parentPosition: Int) -> Int,
    crossinline onBindParentCallback: BaseExpandableAdapter<V1, V2, B>.(holder: BaseViewHolder<V1>, parentItem: B, parentPosition: Int) -> Unit,
    crossinline onBindChildCallback: BaseExpandableAdapter<V1, V2, B>.(holder: BaseViewHolder<V2>, parentItem: B, childPosition: Int) -> Unit
) =
    object : BaseExpandableAdapter<V1, V2, B>(V1::class.java, V2::class.java) {

        override fun getChildCount(parentItem: B, parentPosition: Int) = this.childCountCallback(parentItem, parentPosition)

        override fun onBindParent(holder: BaseViewHolder<V1>, parentItem: B, parentPosition: Int) =
            this.onBindParentCallback(holder, parentItem, parentPosition)

        override fun onBindChild(holder: BaseViewHolder<V2>, parentItem: B, childPosition: Int) =
            this.onBindChildCallback(holder, parentItem, childPosition)
    }

fun <B : Any> createMultiAdapter(list: List<B>? = null) = BaseMultiItemAdapter(list)

fun <B : IContainerBean> createContainerAdapter(list: List<B>? = null) = BaseContainerAdapter(list)