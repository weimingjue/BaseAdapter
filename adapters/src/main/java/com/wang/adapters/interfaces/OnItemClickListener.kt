package com.wang.adapters.interfaces

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.CallSuper
import com.sea.base.adapter.BaseViewHolder
import com.sea.base.ext.view.adapterLayoutPosition
import com.sea.im.base.R
import com.wang.adapters.adapter.BaseAdapter
import com.wang.container.interfaces.IAdapter
import com.wang.container.interfaces.IItemClick
import com.wang.container.interfaces.IListAdapter

/**
 * 点击,长按,header,footer的回调
 */
open class OnItemClickListener : IItemClick {
    @CallSuper //一般不需要重写，所以加了此限制（如果真的不想调用super可以注解抑制掉错误）
    @SuppressLint("MissingSuperCall")
    override fun onClick(view: View) {
        val adapter = getAdapter(view)
        when (val formatPosition = getFormatPosition(adapter, getViewPosition(view))) {
            POSITION_HEADER -> onHeaderClick(view)
            POSITION_FOOTER -> onFooterClick(view)
            else -> onItemClick(view, formatPosition)
        }
    }

    @CallSuper
    @SuppressLint("MissingSuperCall")
    override fun onLongClick(view: View): Boolean {
        val adapter = getAdapter(view)
        return when (val formatPosition = getFormatPosition(adapter, getViewPosition(view))) {
            POSITION_HEADER -> onHeaderLongClick(view)
            POSITION_FOOTER -> onFooterLongClick(view)
            else -> onItemLongClick(view, formatPosition)
        }
    }

    /**
     * 获取当前view所在的adapter
     */
    @CallSuper
    fun getAdapter(view: View): BaseAdapter {
        return view.getTag(R.id.tag_view_adapter) as BaseAdapter
    }

    /**
     * 获取当前view所在的position，注意header、footer
     */
    @CallSuper
    override fun getViewPosition(view: View): Int {
        return getViewHolder(view).adapterLayoutPosition
    }

    /**
     * 根据adapter和绝对position来获取格式化后的position
     *
     * @return [POSITION_HEADER][POSITION_FOOTER]或者0-list.size
     */
    @CallSuper
    fun getFormatPosition(adapter: IAdapter<*>?, position: Int): Int {
        when (adapter) {
            is IListAdapter<*, *, *> -> {
                //listAdapter有header、footer事件
                when {
                    adapter.headerView != null && position == 0 -> {
                        return POSITION_HEADER
                    }

                    adapter.footerView != null && adapter.getItemCount() == position + 1 -> {
                        return POSITION_FOOTER
                    }

                    else -> {
                        if (adapter.headerView != null) {
                            return position - 1
                        }
                        return position
                    }
                }
            }

            else -> {
                //普通adapter
                return position
            }
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
    var onItemClick: ((adapter: BaseAdapter, view: View, listPosition: Int, vh: BaseViewHolder<*>) -> Unit)? = null
    override fun onItemClick(view: View, listPosition: Int) {
        onItemClick?.invoke(getAdapter(view), view, listPosition, getViewHolder(view))
    }

    /**
     * item被长按时
     *
     * @param listPosition list集合所对应的position,不需要-1
     */
    var onItemLongClick: ((adapter: BaseAdapter, view: View, listPosition: Int, vh: BaseViewHolder<*>) -> Boolean)? = null
    override fun onItemLongClick(view: View, listPosition: Int): Boolean {
        return onItemLongClick?.invoke(getAdapter(view), view, listPosition, getViewHolder(view)) ?: false
    }

    // TODO: 调用和子类实现均未处理
    var onItemViewClickWithTag: ((adapter: BaseAdapter, view: View, listPosition: Int, vh: BaseViewHolder<*>) -> Unit)? = null
    override fun onItemViewClickWithTag(view: View, position: Int, tag: String) {
    }

    // TODO: 调用和子类实现均未处理
    var onItemViewLongClickWithTag: ((adapter: BaseAdapter, view: View, listPosition: Int, vh: BaseViewHolder<*>) -> Boolean)? = null
    override fun onItemViewLongClickWithTag(view: View, position: Int, tag: String): Boolean {
        return onItemViewLongClickWithTag?.invoke(getAdapter(view), view, position, getViewHolder(view)) ?: false
    }

    /**
     * 添加的header被点击时,没有可以忽略
     */
    var onHeaderClick: ((adapter: BaseAdapter, view: View) -> Unit)? = null
    open fun onHeaderClick(view: View) {
        onHeaderClick?.invoke(getAdapter(view), view)
    }

    /**
     * 添加的header被长按时,没有可以忽略
     */
    var onHeaderLongClick: ((adapter: BaseAdapter, view: View) -> Boolean)? = null
    open fun onHeaderLongClick(view: View): Boolean {
        return onHeaderLongClick?.invoke(getAdapter(view), view) ?: false
    }

    /**
     * 添加的footer被点击时,没有可以忽略
     */
    var onFooterClick: ((adapter: BaseAdapter, view: View) -> Unit)? = null
    open fun onFooterClick(view: View) {
        onFooterClick?.invoke(getAdapter(view), view)
    }

    /**
     * 添加的footer被长按时,没有可以忽略
     */
    var onFooterLongClick: ((adapter: BaseAdapter, view: View) -> Boolean)? = null
    open fun onFooterLongClick(view: View): Boolean {
        return onFooterLongClick?.invoke(getAdapter(view), view) ?: false
    }

    companion object {
        const val POSITION_HEADER = -1
        const val POSITION_FOOTER = -2
    }
}