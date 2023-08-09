package com.wang.adapters.adapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.sea.base.adapter.BaseViewHolder
import com.wang.adapters.helper.ListAdapterHelper
import com.wang.adapters.interfaces.OnItemClickListener
import com.wang.container.interfaces.IListAdapter

/**
 * 无限循环滑动的adapter
 */
abstract class BaseListCycleAdapter<DB : ViewBinding, BEAN>(
    @LayoutRes layoutId: Int,
    list: List<BEAN>?
) : BaseAdapter(), IListAdapter<BEAN, DB, OnItemClickListener> {
    private val mHelper: ListAdapterHelper<DB, BEAN> = ListAdapterHelper(this, layoutId, list)
    private var mIsCycle = true

    /**
     * 资源id已经不是必须的了
     *
     *
     * 无资源id有2种解决方式（任选其一）：
     * 1.什么都不做，根据泛型自动获取，但Proguard不能混淆[ViewBinding]的子类
     * 2.覆盖[.onCreateViewHolder2]，自己自定义即可
     */
    @JvmOverloads
    constructor(list: List<BEAN>? = null) : this(0, list) {
    }

    override fun getItemCount(): Int {
        if (list.isEmpty()) {
            return 0
        }
        return if (isCycle) Int.MAX_VALUE else listSize()
    }

    override fun onBindViewHolder2(holder: BaseViewHolder<*>, position: Int) {
        //对position进行了%处理
        var p2 = position % list.size
        onBindListViewHolder(holder as BaseViewHolder<DB>, p2, list[p2])
    }

    /**
     * 暂不支持header、footer
     */
    override var headerView: View?
        get() = null
        set(value) {}

    override var footerView: View?
        get() = null
        set(value) {}

    override fun onCreateViewHolder2(parent: ViewGroup, viewType: Int): BaseViewHolder<DB> {
        return onCreateListViewHolder(parent)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // list相关的方法，其他方法请使用getList进行操作
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override val list get() = mHelper.list

    /**
     * 获取指定bean
     */
    override fun getItemData(listPosition: Int): BEAN {
        return super.getItemData(listPosition % list.size)
    }

    override fun getItemDataOrNull(listPosition: Int): BEAN? {
        return super.getItemDataOrNull(listPosition % list.size)
    }
    ///////////////////////////////////////////////////////////////////////////
    // 以下是增加的方法
    ///////////////////////////////////////////////////////////////////////////
    /**
     * 最终你的list的create
     *
     *
     * 默认用DataBinding create
     * 完全不需要的话覆盖整个方法就行了，不会出问题
     * 你也可以重写来添加自己的默认逻辑，如：全局隐藏显示、嵌套rv的默认属性设置等
     */
    override fun onCreateListViewHolder(parent: ViewGroup): BaseViewHolder<DB> {
        return mHelper.onCreateDefaultViewHolder(parent, this)
    }

    /**
     * @param isCycle true 默认值，可循环滑动
     */
    var isCycle: Boolean
        get() = mIsCycle
        set(isCycle) {
            if (isCycle != mIsCycle) {
                mIsCycle = isCycle
                notifyDataSetChanged()
            }
        }

}