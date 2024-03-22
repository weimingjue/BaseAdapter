package com.wang.adapters.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wang.adapters.interfaces.IHeaderFooterListAdapter


/**
 * 两个position有点头疼，无从选择，合并成一个
 *
 * 注意点：
 * list里注意header、footer
 * 完全就没bind过，肯定还是-1了
 */
inline val RecyclerView.ViewHolder.adapterLayoutPosition: Int get() = if (layoutPosition < 0) bindingAdapterPosition else layoutPosition

/**
 * 获取list的真正position
 */
inline val RecyclerView.ViewHolder.listPosition: Int get() = getListPosition(bindingAdapter)

@Suppress("NOTHING_TO_INLINE")
inline fun RecyclerView.ViewHolder.getListPosition(adapter: RecyclerView.Adapter<*>?) =
    if (adapter is IHeaderFooterListAdapter<*>)
        adapterLayoutPosition - adapter.headerViewCount
    else
        adapterLayoutPosition

inline fun RecyclerView.ViewHolder.setOnClickListener(crossinline block: (View) -> Unit) {
    this.itemView.setOnClickListener { block.invoke(it) }
}

inline fun RecyclerView.ViewHolder.setOnLongClickListener(
    checkPosition: Boolean = true,
    crossinline block: (View) -> Boolean
) {
    this.itemView.setOnLongClickListener {
        run {
            if (checkPosition && this.listPosition < 0) {
                return@run false
            }
            block.invoke(it)
        }
    }
}

/**
 * 由于点击事件是延迟触发的，在此期间有极小概率触发adapter刷新导致position为-1，此处对position进行了判断
 */
@JvmOverloads
inline fun RecyclerView.ViewHolder.setOnFastClickListener(
    checkPosition: Boolean = true,
    clickInterval: Long = 300,
    crossinline block: (View) -> Unit
) {
    this.itemView.setOnFastClickListener(clickInterval) {
        run {
            if (checkPosition && this.listPosition < 0) {
                return@run
            }
            block.invoke(it)
        }
    }
}