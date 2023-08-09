package com.wang.adapters.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sea.base.adapter.BaseViewHolder
import com.sea.im.base.R
import com.wang.adapters.R
import com.wang.adapters.interfaces.OnItemClickListener
import com.wang.container.holder.BaseViewHolder
import com.wang.container.interfaces.IAdapter

/**
 * 适用于rv
 * 增加点击事件
 */
abstract class BaseAdapter : RecyclerView.Adapter<BaseViewHolder<*>>(),
    IAdapter<OnItemClickListener> {
    val TAG: String = javaClass.simpleName
    override var abstractClickListener: OnItemClickListener? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val holder = onCreateViewHolder2(parent, viewType)
        holder.itemView.setTag(R.id.tag_view_holder, holder)
        holder.itemView.setTag(R.id.tag_view_adapter, this)
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        //设置点击事件
        holder.itemView.setOnClickListener(abstractClickListener)
        holder.itemView.setOnLongClickListener(abstractClickListener)
        //防止null时抢占事件
        holder.itemView.isClickable = abstractClickListener != null
        holder.itemView.isLongClickable = abstractClickListener != null
        onBindViewHolder2(holder, position)
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是可能用到的父类方法
    ///////////////////////////////////////////////////////////////////////////

    //    @Override
    //    public int getItemViewType(int position) {
    //        return 0;
    //    }
    protected abstract fun onBindViewHolder2(holder: BaseViewHolder<*>, position: Int)
    protected abstract fun onCreateViewHolder2(parent: ViewGroup, viewType: Int): BaseViewHolder<*>

    override fun getItemViewType(position: Int): Int {
        return super<IAdapter>.getItemViewType(position)
    }

    abstract override fun getItemCount(): Int
}