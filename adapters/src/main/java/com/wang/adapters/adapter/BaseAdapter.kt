package com.wang.adapters.adapter

import androidx.recyclerview.widget.RecyclerView
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.interfaces.IAdapter
import com.wang.adapters.utils.createBaseAdapter
import com.wang.adapters.utils.createListAdapter

/**
 * 适用于rv
 * 创建方法见[createBaseAdapter]、[createListAdapter]等
 */
abstract class BaseAdapter : RecyclerView.Adapter<BaseViewHolder<*>>(), IAdapter {
    ///////////////////////////////////////////////////////////////////////////
    // 以下是可能用到的父类方法
    ///////////////////////////////////////////////////////////////////////////

    //    @Override
    //    public int getItemViewType(int position) {
    //        return 0;
    //    }

    abstract override fun getItemCount(): Int
}