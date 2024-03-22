package com.wang.adapters.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.wang.adapters.utils.findRootVbByTag

class BaseViewHolder<T : ViewBinding> : RecyclerView.ViewHolder {
    constructor(itemView: View) : super(itemView) {
        _vb = itemView.findRootVbByTag()
    }

    constructor(vb: T) : super(vb.root) {
        this._vb = vb
    }

    private val _vb: T?
    val vb get() = _vb ?: throw IllegalArgumentException("没有找到ViewBinding，请确认是否使用了ViewBinding：$itemView")

}