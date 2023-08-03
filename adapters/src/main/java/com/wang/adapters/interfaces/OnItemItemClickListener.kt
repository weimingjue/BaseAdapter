package com.wang.adapters.interfaces

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.wang.adapters.R
import com.wang.adapters.adapter.BaseAdapter
import com.wang.container.holder.BaseViewHolder
import com.wang.container.interfaces.IAdapter
import com.zhy.view.flowlayout.FlowLayout
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * 高级功能:adapter套adapter的点击事件,具体用法见实现类
 * 方法回调见onParent...和onChild...，其他方法不要重写
 *
 *
 * create时请设置好adapter和layoutManager
 * bind里写法[BaseAdapter.setItemRvData]
 */
interface OnItemItemClickListener : OnItemClickListener {
    @IntDef(ITEM_CLICK, ITEM_LONG_CLICK, ITEM_HEADER_CLICK, ITEM_HEADER_LONG_CLICK, ITEM_FOOTER_CLICK, ITEM_FOOTER_LONG_CLICK)
    @Retention(RetentionPolicy.SOURCE)
    annotation class ClickType  //该变量只能传入上面几种,否则会报错

    @CallSuper //一般不需要重写，所以加了此限制（如果真的不想调用super可以注解抑制掉错误）
    override fun onItemClick(view: View, listPosition: Int) {
        performClick(view, ITEM_CLICK, listPosition)
    }

    @CallSuper
    override fun onItemLongClick(view: View, listPosition: Int): Boolean {
        return performClick(view, ITEM_LONG_CLICK, listPosition)
    }

    @CallSuper
    override fun onHeaderClick(view: View) {
        performClick(view, ITEM_HEADER_CLICK, 0)
    }

    @CallSuper
    override fun onHeaderLongClick(view: View): Boolean {
        return performClick(view, ITEM_HEADER_LONG_CLICK, 0)
    }

    @CallSuper
    override fun onFooterClick(view: View) {
        performClick(view, ITEM_FOOTER_CLICK, 0)
    }

    @CallSuper
    override fun onFooterLongClick(view: View): Boolean {
        return performClick(view, ITEM_FOOTER_LONG_CLICK, 0)
    }

    /**
     * 总的点击分发
     */
    @CallSuper
    fun performClick(view: View, @ClickType clickType: Int, listPosition: Int): Boolean {
        val parentAdapter = getParentAdapter(view)
        return if (parentAdapter == null) {
            when (clickType) {
                ITEM_CLICK -> {
                    onParentItemClick(view, listPosition)
                    true
                }

                ITEM_LONG_CLICK -> onParentItemLongClick(view, listPosition)
                ITEM_HEADER_CLICK -> {
                    onParentHeaderClick(view)
                    true
                }

                ITEM_HEADER_LONG_CLICK -> onParentHeaderLongClick(view)
                ITEM_FOOTER_CLICK -> {
                    onParentFooterClick(view)
                    true
                }

                ITEM_FOOTER_LONG_CLICK -> onParentFooterLongClick(view)
                else -> false
            }
        } else {
            val parentPosition = getParentViewHolder(view).commonPosition
            val formatParentPosition = getFormatPosition(parentAdapter, parentPosition)
            when (formatParentPosition) {
                OnItemClickListener.Companion.POSITION_HEADER, OnItemClickListener.Companion.POSITION_FOOTER -> false //item点击，暂不支持header、footer里的RecyclerView
                else -> when (clickType) {
                    ITEM_CLICK -> {
                        onChildItemClick(view, formatParentPosition, listPosition)
                        true
                    }

                    ITEM_LONG_CLICK -> onChildItemLongClick(view, formatParentPosition, listPosition)
                    ITEM_HEADER_CLICK -> {
                        onChildHeaderClick(view, formatParentPosition)
                        true
                    }

                    ITEM_HEADER_LONG_CLICK -> onChildHeaderLongClick(view, formatParentPosition)
                    ITEM_FOOTER_CLICK -> {
                        onChildFooterClick(view, formatParentPosition)
                        true
                    }

                    ITEM_FOOTER_LONG_CLICK -> onChildFooterLongClick(view, formatParentPosition)
                    else -> false
                }
            }
        }
    }

    /**
     * 外层的position需要遍历
     */
    @CallSuper
    fun getParentAdapter(view: View): IAdapter<*>? {
        var parent = view.parent as? ViewGroup
        while (parent != null) {
            //第二层不建议使用ListView或GridView(肯定没有复用性,并且效率很差,可以尝试使用RecyclerView然后wrap)
//            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout || parent instanceof AdapterView) {
            if (parent is RecyclerView || parent is ViewPager || parent is FlowLayout) {
                return parent.getTag(R.id.tag_view_adapter) as IAdapter<*>
            }
            parent = parent.parent
        }
        //没取到返回null
        return null
    }

    /**
     * 获取当前view所在的ViewHolder
     */
    @CallSuper
    fun getParentViewHolder(view: View): BaseViewHolder<*>? {
        var parent = view.parent as? ViewGroup
        while (parent != null) {
            //第二层不建议使用ListView或GridView(肯定没有复用性,并且效率很差,可以尝试使用RecyclerView然后wrap)
//            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout || parent instanceof AdapterView) {
            if (parent is RecyclerView || parent is ViewPager || parent is FlowLayout) {
                return parent.getTag(R.id.tag_view_holder) as BaseViewHolder<*>
            }
            parent = parent.parent
        }
        //没取到返回null
        return null
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是parent的回调
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 当外层被点击时
     *
     * @param parentPosition 外层adapter的position
     */
    fun onParentItemClick(view: View, parentPosition: Int)

    /**
     * 当外层被长按时
     *
     * @param parentPosition 外层adapter的position
     */
    fun onParentItemLongClick(view: View, parentPosition: Int): Boolean {
        return false
    }

    fun onParentHeaderClick(view: View) {}
    fun onParentHeaderLongClick(view: View): Boolean {
        return false
    }

    fun onParentFooterClick(view: View) {}
    fun onParentFooterLongClick(view: View): Boolean {
        return false
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是child的回调
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 当内层被点击时
     *
     * @param parentPosition 外层adapter对应的position
     * @param childPosition  内层adapter对应的position
     */
    fun onChildItemClick(view: View, parentPosition: Int, childPosition: Int)

    /**
     * 当内层被长按时
     *
     * @param parentPosition 外层adapter对应的position
     * @param childPosition  内层adapter对应的position
     */
    fun onChildItemLongClick(view: View, parentPosition: Int, childPosition: Int): Boolean {
        return false
    }

    fun onChildHeaderClick(view: View, parentPosition: Int) {}
    fun onChildHeaderLongClick(view: View, parentPosition: Int): Boolean {
        return false
    }

    fun onChildFooterClick(view: View, parentPosition: Int) {}
    fun onChildFooterLongClick(view: View, parentPosition: Int): Boolean {
        return false
    }

    companion object {
        const val ITEM_CLICK = 0
        const val ITEM_LONG_CLICK = 1
        const val ITEM_HEADER_CLICK = 2
        const val ITEM_HEADER_LONG_CLICK = 3
        const val ITEM_FOOTER_CLICK = 4
        const val ITEM_FOOTER_LONG_CLICK = 5
    }
}