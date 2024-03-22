package com.wang.adapters.adapter

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.wang.adapters.helper.ListAdapterHelper
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.interfaces.IListAdapter
import com.wang.adapters.utils.listPosition

/**
 * 无限循环滑动的adapter
 */
abstract class BaseListCycleAdapter<VB : ViewBinding, BEAN : Any>(list: List<BEAN>?) : BaseAdapter(), IListAdapter<BEAN> {
    private val mHelper = ListAdapterHelper(this, list)

    override fun getItemCount(): Int {
        if (list.isEmpty()) {
            return 0
        }
        return if (isCycle) Int.MAX_VALUE else listSize()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        onBindListViewHolder(holder as BaseViewHolder<VB>, list[position % list.size])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        return onCreateListViewHolder(parent)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // list相关的方法，其他方法请使用getList进行操作
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override val list get() = mHelper.list

    /**
     * 获取指定bean
     */
    override fun getItemData(listPosition: Int): BEAN {
        return super.getItemData(listPosition % list.size)
    }

    override fun getItemDataOrNull(listPosition: Int): BEAN? {
        return super.getItemDataOrNull(listPosition % list.size)
    }
    ///////////////////////////////////////////////////////////////////////////
    // 以下是增加的方法
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 最终你的list的create
     *
     *
     * 默认用DataBinding create
     * 完全不需要的话覆盖整个方法就行了，不会出问题
     * 你也可以重写来添加自己的默认逻辑，如：全局隐藏显示、嵌套rv的默认属性设置等
     */
    open fun onCreateListViewHolder(parent: ViewGroup) = mHelper.onCreateDefaultViewHolder<VB>(parent, this)

    /**
     * 最终你的list的bind
     * @param holder position见[cycleListPosition]
     */
    abstract fun onBindListViewHolder(holder: BaseViewHolder<VB>, bean: BEAN)

    /**
     * true 默认值，可循环滑动
     */
    var isCycle: Boolean = true
        set(isCycle) {
            if (field != isCycle) {
                field = isCycle
                notifyDataSetChanged()
            }
        }

    /**
     * 对position进行取余
     */
    inline val BaseViewHolder<VB>.cycleListPosition get() = listPosition % list.size
}