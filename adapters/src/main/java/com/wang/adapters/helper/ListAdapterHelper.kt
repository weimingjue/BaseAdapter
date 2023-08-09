package com.wang.adapters.helper

import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.sea.base.adapter.BaseViewHolder
import com.sea.base.ext.view.inflater
import com.sea.base.ext.view.layoutInflater
import com.sea.base.utils.ViewBindingHelper
import com.wang.container.helper.BaseListAdapterHelper
import com.wang.container.interfaces.IListAdapter

/**
 * 适用于简单list样式的公共代码
 */
class ListAdapterHelper<VB : ViewBinding, BEAN>(
    adapter: IListAdapter<BEAN, *, *>,
    @LayoutRes
    val mLayoutId: Int,
    list: List<BEAN>?
) : BaseListAdapterHelper<BEAN>(adapter, list) {
    @IntDef(TYPE_BODY, TYPE_HEADER, TYPE_FOOTER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class AdapterListType  //该变量只能传入上面几种,否则会报错

    fun onCreateViewHolder(parent: ViewGroup, @AdapterListType viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_HEADER, TYPE_FOOTER -> onCreateHeaderFooterViewHolder(parent)
            TYPE_BODY -> adapter.onCreateListViewHolder(parent)
            else -> adapter.onCreateListViewHolder(parent)
        }
    }

    fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                onBindHeaderFooterViewHolder(holder, headerView!!)
                return
            }

            TYPE_FOOTER -> {
                onBindHeaderFooterViewHolder(holder, footerView!!)
                return
            }

            TYPE_BODY -> {
                val listPosition = position - adapter.headerViewCount
                adapter.onBindListViewHolder(
                    holder as BaseViewHolder<ViewBinding>,
                    listPosition,
                    list[listPosition]
                )
            }

            else -> {
                val listPosition = position - adapter.headerViewCount
                adapter.onBindListViewHolder(
                    holder as BaseViewHolder<ViewBinding>,
                    listPosition,
                    list[listPosition]
                )
            }
        }
    }

    @AdapterListType
    fun getItemViewType(position: Int): Int {
        if (adapter.isHeaderView && position == 0) {
            return TYPE_HEADER
        }
        if (adapter.isFooterView && adapter.getItemCount() == position + 1) {
            return TYPE_FOOTER
        }
        return TYPE_BODY
    }

    fun onCreateDefaultViewHolder(
        parent: ViewGroup,
        obj: Any
    ): BaseViewHolder<VB> {
        return if (mLayoutId == 0)
            BaseViewHolder(
                ViewBindingHelper.getViewBindingInstance<VB>(obj, parent.layoutInflater, parent)
            )
        else
            BaseViewHolder(parent.inflater(mLayoutId))
    }

    companion object {
        const val TYPE_BODY = 0
        const val TYPE_HEADER = 1
        const val TYPE_FOOTER = 2
    }
}