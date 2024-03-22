package com.wang.adapters.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.wang.adapters.R

/**
 * 获取见[findVbByTag]
 */
fun ViewBinding.saveVbToTag() {
    this.root.setTag(R.id.tag_view_binding_obj, this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Context.layoutInflate(): LayoutInflater = LayoutInflater.from(this)

/**
 * 倒序遍历
 */
inline fun <T> List<T>.forEachReverseSequence(action: (index: Int, T) -> Unit) {
    for (index in downIndices) {
        action(index, this[index])
    }
}

val Collection<*>.downIndices: IntProgression
    get() = size - 1 downTo 0

/**
 * 如果有就直接返回，如果没有就创建并设置，一般用于被嵌套的RecyclerView中
 */
inline fun <reified T : RecyclerView.Adapter<*>> RecyclerView.getAdapterOrCreate(createCallback: () -> T): T {
    return (adapter as? T) ?: createCallback.invoke().also { adapter = it }
}

/**
 * 如果有就直接返回，如果没有就创建并设置，一般用于被嵌套的RecyclerView中
 */
inline fun <reified T : RecyclerView.LayoutManager> RecyclerView.getLayoutManagerOrCreate(createCallback: () -> T): T {
    return (layoutManager as? T) ?: createCallback.invoke().also { layoutManager = it }
}

/**
 * 设置[GridLayoutManager]的spanSizeLookup，如果不是GridLayoutManager则会直接崩溃
 */
inline fun RecyclerView.setGridLayoutManagerSpanSizeLookup(crossinline createCallback: (absPosition: Int) -> Int) {
    (this.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int) = createCallback.invoke(position)
    }
}