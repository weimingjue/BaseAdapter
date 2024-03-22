package com.wang.adapters.utils

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.wang.adapters.R

/**
 * 避免快速点击
 */
@JvmOverloads
inline fun <T : View> T.setOnFastClickListener(
    clickInterval: Long = 300,
    crossinline block: (T) -> Unit
) {
    setOnClickListener(object : OnClickListener {
        var timestamp = 0L
        override fun onClick(v: View) {
            val now = System.currentTimeMillis()
            if (isClickable && now - timestamp >= clickInterval) {
                block(this@setOnFastClickListener)
            }
            timestamp = now
        }
    })
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewBinding> View.findVbByTag() = rootView.getTypeTag<T>(R.id.tag_view_binding_obj)

/**
 * 根据root找到对应的vb（必须使用[ViewBindingHelper]相关方法或者手动调用[saveVbToTag]才会find到）
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : ViewBinding> View.findRootVbByTag() = rootView.findVbByTag<T>()

@Suppress("NOTHING_TO_INLINE")
inline fun <T> View.getTypeTag(@IdRes id: Int): T? = getTag(id) as? T

inline val View.layoutInflater: LayoutInflater get() = context.layoutInflate()

const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

private val dlpMethod = run {
    val method = ViewGroup::class.java.getDeclaredMethod("generateDefaultLayoutParams")
    method.isAccessible = true
    return@run method
}

/**
 * [ViewGroup.generateDefaultLayoutParams]是protected的，所以有此拓展
 */
fun ViewGroup.getDefLayoutParams(): ViewGroup.LayoutParams {
    return if (this is RecyclerView) {
        this.layoutManager?.generateDefaultLayoutParams() ?: RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    } else {
        dlpMethod.invoke(this) as ViewGroup.LayoutParams
    }
}