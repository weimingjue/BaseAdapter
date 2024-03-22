package com.wang.adapters.interfaces

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes

/**
 * 所有list的adapter的接口
 */
interface IHeaderFooterListAdapter<BEAN : Any> : IListAdapter<BEAN> {

    /**
     * 支持手动设置FrameLayout.LayoutParams的属性：with、height、margin，不支持gravity
     * 如果没有Params则默认宽高为match、wrap
     */
    var headerView: View?
    var footerView: View?

    override val headerViewCount get() = if (hasHeaderView) 1 else 0
    override val hasHeaderView get() = headerView != null
    override val footerViewCount get() = if (hasFooterView) 1 else 0
    override val hasFooterView get() = footerView != null

    fun removeHeaderView() {
        headerView = null
    }

    fun removeFooterView() {
        footerView = null
    }

    fun setHeaderView(context: Context, @LayoutRes layoutRes: Int) {
        if (layoutRes == 0) {
            removeHeaderView()
            return
        }
        headerView =
            LayoutInflater.from(context).inflate(layoutRes, FrameLayout(context), false)
    }

    fun setFooterView(context: Context, @LayoutRes layoutRes: Int) {
        if (layoutRes == 0) {
            removeFooterView()
            return
        }
        footerView =
            LayoutInflater.from(context).inflate(layoutRes, FrameLayout(context), false)
    }
}