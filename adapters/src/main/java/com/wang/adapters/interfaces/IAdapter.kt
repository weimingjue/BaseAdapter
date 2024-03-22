package com.wang.adapters.interfaces

import android.view.ViewGroup
import com.wang.adapters.holder.BaseViewHolder

/**
 * 所有adapter的接口
 */
interface IAdapter {
    fun getItemCount(): Int
    fun getItemViewType(position: Int): Int
    fun createViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>
    fun bindViewHolder(holder: BaseViewHolder<*>, position: Int)

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // notify相关方法
    /////////////////////////////////////////////////////////////////////////////////////////////////

    fun notifyDataSetChanged()

    /**
     * 也支持remove或insert最后一条（不建议使用）
     */
    fun notifyItemChanged(position: Int)

    /**
     * 如果出现原数据和新数据size不相等，请分别调用其他方法，不要单独使用changed
     */
    fun notifyItemRangeChanged(positionStart: Int, itemCount: Int)

    fun notifyItemInserted(position: Int)

    fun notifyItemRangeInserted(positionStart: Int, itemCount: Int)

    /**
     * 移动规则：先移除[fromPosition]，移除完成后再添加到[toPosition]，在list里相当于remoteAt(fromPosition)然后add(toPosition)
     */
    fun notifyItemMoved(fromPosition: Int, toPosition: Int)

    fun notifyItemRemoved(position: Int)

    fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int)
}