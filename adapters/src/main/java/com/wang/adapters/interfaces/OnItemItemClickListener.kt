package com.wang.adapters.interfaces

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.flexbox.FlexboxLayout
import com.sea.base.adapter.BaseViewHolder
import com.sea.base.ext.view.adapterLayoutPosition
import com.sea.im.base.R
import com.wang.adapters.adapter.BaseAdapter
import com.wang.container.interfaces.IAdapter

/**
 * 高级功能:adapter套adapter的点击事件,具体用法见实现类
 * 方法回调见onParent...和onChild...，其他方法不要重写
 *
 * create时请设置好adapter和layoutManager
 */
open class OnItemItemClickListener : OnItemClickListener() {

    @CallSuper //一般不需要重写，所以加了此限制（如果真的不想调用super可以注解抑制掉错误）
    override fun onItemClick(view: View, listPosition: Int) {
        checkParentInfo(
            view,
            noP = {
                onParentItemClick(view, listPosition)
            },
            hasP = { parentListPosition ->
                onChildItemClick(view, parentListPosition, listPosition)
            })
    }

    @CallSuper
    override fun onItemLongClick(view: View, listPosition: Int): Boolean {
        checkParentInfo(
            view,
            noP = {
                return onParentItemLongClick(view, listPosition)
            },
            hasP = { parentListPosition ->
                return onChildItemLongClick(view, parentListPosition, listPosition)
            })
        return false
    }

    @CallSuper
    override fun onHeaderClick(view: View) {
        checkParentInfo(
            view,
            noP = {
                onParentHeaderClick(view)
            },
            hasP = { parentListPosition ->
                onChildHeaderClick(view, parentListPosition)
            })
    }

    @CallSuper
    override fun onHeaderLongClick(view: View): Boolean {
        checkParentInfo(
            view,
            noP = {
                return onParentHeaderLongClick(view)
            },
            hasP = { parentListPosition ->
                return onChildHeaderLongClick(view, parentListPosition)
            })
        return false
    }

    @CallSuper
    override fun onFooterClick(view: View) {
        checkParentInfo(
            view,
            noP = {
                onParentFooterClick(view)
            },
            hasP = { parentListPosition ->
                onChildFooterClick(view, parentListPosition)
            })
    }

    @CallSuper
    override fun onFooterLongClick(view: View): Boolean {
        checkParentInfo(
            view,
            noP = {
                return onParentFooterLongClick(view)
            },
            hasP = { parentListPosition ->
                return onChildFooterLongClick(view, parentListPosition)
            })
        return false
    }

    private inline fun checkParentInfo(view: View, noP: () -> Unit, hasP: (parentListPosition: Int) -> Unit) {
        val parentAdapter = getParentAdapter(view)
        if (parentAdapter == null) {
            noP.invoke()
        } else {
            val parentPosition = getParentViewHolder(view)?.adapterLayoutPosition ?: -1
            when (val formatParentPosition = getFormatPosition(parentAdapter, parentPosition)) {
                POSITION_HEADER, POSITION_FOOTER -> return //item点击，暂不支持header、footer里的RecyclerView
                else -> {
                    hasP.invoke(formatParentPosition)
                }
            }
        }
    }

    /**
     * 外层的position需要遍历
     */
    private fun getParentAdapter(view: View): IAdapter<*>? {
        var parent = view.parent as? ViewGroup
        while (parent != null) {
            //第二层不建议使用ListView或GridView(肯定没有复用性,并且效率很差,可以尝试使用RecyclerView然后wrap)
//            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout || parent instanceof AdapterView) {
            if (parent is RecyclerView || parent is ViewPager || parent is FlexboxLayout) {
                return parent.getTag(R.id.tag_view_adapter) as IAdapter<*>
            }
            parent = parent.parent as? ViewGroup
        }
        //没取到返回null
        return null
    }

    /**
     * 获取当前view所在的ViewHolder
     */
    private fun getParentViewHolder(view: View): BaseViewHolder<*>? {
        var parent = view.parent as? ViewGroup
        while (parent != null) {
            //第二层不建议使用ListView或GridView(肯定没有复用性,并且效率很差,可以尝试使用RecyclerView然后wrap)
//            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout || parent instanceof AdapterView) {
            if (parent is RecyclerView || parent is ViewPager || parent is FlexboxLayout) {
                return parent.getTag(R.id.tag_view_holder) as BaseViewHolder<*>
            }
            parent = parent.parent as? ViewGroup
        }
        //没取到返回null
        return null
    }

    open fun onParentItemClick(view: View, parentListPosition: Int) {
        onItemClick?.invoke(getAdapter(view), view, parentListPosition, getViewHolder(view))
    }

    open fun onParentItemLongClick(view: View, parentListPosition: Int): Boolean {
        return onItemLongClick?.invoke(getAdapter(view), view, parentListPosition, getViewHolder(view)) ?: false
    }

    open fun onParentHeaderClick(view: View) {
        onHeaderClick?.invoke(getAdapter(view), view)
    }

    open fun onParentHeaderLongClick(view: View): Boolean {
        return onHeaderLongClick?.invoke(getAdapter(view), view) ?: false
    }

    open fun onParentFooterClick(view: View) {
        onFooterClick?.invoke(getAdapter(view), view)
    }

    open fun onParentFooterLongClick(view: View): Boolean {
        return onFooterLongClick?.invoke(getAdapter(view), view) ?: false
    }

    var onChildItemClick: ((adapter: BaseAdapter, view: View, parentListPosition: Int, childListPosition: Int, vh: BaseViewHolder<*>) -> Unit)? = null
    open fun onChildItemClick(view: View, parentListPosition: Int, childListPosition: Int) {
        onChildItemClick?.invoke(getAdapter(view), view, parentListPosition, childListPosition, getViewHolder(view))
    }

    var onChildItemLongClick: ((adapter: BaseAdapter, view: View, parentListPosition: Int, childListPosition: Int, vh: BaseViewHolder<*>) -> Boolean)? = null
    open fun onChildItemLongClick(view: View, parentListPosition: Int, childListPosition: Int): Boolean {
        return onChildItemLongClick?.invoke(getAdapter(view), view, parentListPosition, childListPosition, getViewHolder(view)) ?: false
    }

    var onChildHeaderClick: ((adapter: BaseAdapter, view: View, parentListPosition: Int) -> Unit)? = null
    open fun onChildHeaderClick(view: View, parentListPosition: Int) {
        onChildHeaderClick?.invoke(getAdapter(view), view, parentListPosition)
    }

    var onChildHeaderLongClick: ((adapter: BaseAdapter, view: View, parentListPosition: Int) -> Boolean)? = null
    open fun onChildHeaderLongClick(view: View, parentListPosition: Int): Boolean {
        return onChildHeaderLongClick?.invoke(getAdapter(view), view, parentListPosition) ?: false
    }

    var onChildFooterClick: ((adapter: BaseAdapter, view: View, parentListPosition: Int) -> Unit)? = null
    open fun onChildFooterClick(view: View, parentListPosition: Int) {
        onChildFooterClick?.invoke(getAdapter(view), view, parentListPosition)
    }

    var onChildFooterLongClick: ((adapter: BaseAdapter, view: View, parentListPosition: Int) -> Boolean)? = null
    open fun onChildFooterLongClick(view: View, parentListPosition: Int): Boolean {
        return onChildFooterLongClick?.invoke(getAdapter(view), view, parentListPosition) ?: false
    }
}