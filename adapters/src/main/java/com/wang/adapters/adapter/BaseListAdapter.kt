package com.wang.adapters.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.wang.adapters.helper.ListAdapterHelper
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.interfaces.IHeaderFooterListAdapter
import com.wang.adapters.utils.listPosition

/**
 * RecyclerView listAdapter的基类
 * 可以加header、footer,如果是Grid需要自行处理setSpanSizeLookup头尾的跨度
 * [notifyItemChanged]相关方法时注意有header时需要+1，建议使用[notifyListItemChanged]等相关方法
 *
 * 见[BaseAdapter.create]等方法
 */
abstract class BaseListAdapter<VB : ViewBinding, BEAN : Any>(list: List<BEAN>? = null) : BaseAdapter(), IHeaderFooterListAdapter<BEAN> {

    private val listHelper = ListAdapterHelper(this, list)

    override val list get() = listHelper.list

    override var headerView: View?
        get() = listHelper.headerView
        set(value) {
            listHelper.headerView = value
        }
    override var footerView: View?
        get() = listHelper.footerView
        set(value) {
            listHelper.footerView = value
        }

    override fun getItemCount() = headerViewCount + footerViewCount + listSize()

    @ListAdapterHelper.AdapterListType
    override fun getItemViewType(position: Int) = listHelper.getItemViewType(position)

    override fun onCreateViewHolder(parent: ViewGroup, @ListAdapterHelper.AdapterListType viewType: Int) =
        when (viewType) {
            ListAdapterHelper.TYPE_HEADER, ListAdapterHelper.TYPE_FOOTER -> listHelper.onCreateHeaderFooterViewHolder(parent)
            //TYPE_BODY
            else -> onCreateListViewHolder(parent)
        }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (getItemViewType(position)) {
            ListAdapterHelper.TYPE_HEADER -> {
                listHelper.onBindHeaderFooterViewHolder(holder, headerView!!)
            }

            ListAdapterHelper.TYPE_FOOTER -> {
                listHelper.onBindHeaderFooterViewHolder(holder, footerView!!)
            }

            else -> {//TYPE_BODY
                onBindListViewHolder(
                    holder as BaseViewHolder<VB>,
                    list[holder.listPosition]
                )
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是增加的方法
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 最终你的list的create
     *
     * 默认用DataBinding create
     * 完全不需要的话覆盖整个方法就行了，不会出问题
     * 你也可以重写来添加自己的默认逻辑，如：全局隐藏显示、嵌套rv的默认属性设置等
     */
    open fun onCreateListViewHolder(parent: ViewGroup) = listHelper.onCreateDefaultViewHolder<VB>(parent, this)

    /**
     * 最终你的list的bind
     */
    abstract fun onBindListViewHolder(holder: BaseViewHolder<VB>, bean: BEAN)
}