package com.wang.adapters.interfaces

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.CallSuper
import com.wang.container.interfaces.IAdapter
import com.wang.container.interfaces.IItemClick
import com.wang.container.interfaces.IListAdapter

/**
 * 点击,长按,header,footer的回调
 */
interface OnItemClickListener : IItemClick {
    @CallSuper //一般不需要重写，所以加了此限制（如果真的不想调用super可以注解抑制掉错误）
    @SuppressLint("MissingSuperCall")
    override fun onClick(view: View) {
        val adapter = getAdapter(view)
        val formatPosition = getFormatPosition(adapter, getViewPosition(view))
        when (formatPosition) {
            POSITION_HEADER -> onHeaderClick(view)
            POSITION_FOOTER -> onFooterClick(view)
            else -> onItemClick(view, formatPosition)
        }
    }

    @CallSuper
    @SuppressLint("MissingSuperCall")
    override fun onLongClick(view: View): Boolean {
        val adapter = getAdapter(view)
        val formatPosition = getFormatPosition(adapter, getViewPosition(view))
        return when (formatPosition) {
            POSITION_HEADER -> onHeaderLongClick(view)
            POSITION_FOOTER -> onFooterLongClick(view)
            else -> onItemLongClick(view, formatPosition)
        }
    }

    /**
     * 获取当前view所在的position，注意header、footer
     */
    @CallSuper
    override fun getViewPosition(view: View): Int {
        return getViewHolder(view).commonPosition
    }

    /**
     * 根据adapter和绝对position来获取格式化后的position
     *
     * @return [.POSITION_HEADER][.POSITION_FOOTER]或者0-list.size
     */
    @CallSuper
    fun getFormatPosition(adapter: IAdapter<*>?, position: Int): Int {
        var position = position
        return if (adapter is IListAdapter<*, *, *>) {
            //listAdapter有header、footer事件
            val listAdapter = adapter
            if (listAdapter.headerView != null && position == 0) {
                POSITION_HEADER
            } else if (listAdapter.footerView != null && listAdapter.itemCount == position + 1) {
                POSITION_FOOTER
            } else {
                if (listAdapter.headerView != null) {
                    position--
                }
                position
            }
        } else {
            //普通adapter
            position
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是item,header,footer的点击和长按回调
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * item被点击时
     *
     * @param listPosition list集合所对应的position,不需要-1
     */
    override fun onItemClick(view: View, listPosition: Int)

    /**
     * item被长按时
     *
     * @param listPosition list集合所对应的position,不需要-1
     */
    override fun onItemLongClick(view: View, listPosition: Int): Boolean {
        return false
    }

    /**
     * 添加的header被点击时,没有可以忽略
     */
    fun onHeaderClick(view: View) {}

    /**
     * 添加的header被长按时,没有可以忽略
     */
    fun onFooterLongClick(view: View): Boolean {
        return false
    }

    /**
     * 添加的footer被点击时,没有可以忽略
     */
    fun onFooterClick(view: View) {}

    /**
     * 添加的footer被长按时,没有可以忽略
     */
    fun onHeaderLongClick(view: View): Boolean {
        return false
    }

    companion object {
        const val POSITION_HEADER = -1
        const val POSITION_FOOTER = -2
    }
}