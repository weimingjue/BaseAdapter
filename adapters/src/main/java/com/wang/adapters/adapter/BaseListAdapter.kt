package com.wang.adapters.adapter

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.sea.base.adapter.BaseViewHolder
import com.wang.adapters.helper.ListAdapterHelper
import com.wang.adapters.helper.ListAdapterHelper.AdapterListType
import com.wang.adapters.interfaces.OnItemClickListener
import com.wang.container.interfaces.IListAdapter

/**
 * RecyclerView listAdapter的基类
 * 可以加header、footer,如果是Grid需要自行处理setSpanSizeLookup头尾的跨度
 * [.notifyItemChanged]相关方法时注意有header时需要+1
 *
 *
 * 多条目见[BaseAdapterMultipleList]
 */
abstract class BaseListAdapter<DB : ViewBinding, BEAN>(
    @LayoutRes layoutId: Int,
    list: List<BEAN>? = null
) : BaseAdapter(), IListAdapter<BEAN, DB, OnItemClickListener> {
    private val listHelper: ListAdapterHelper<DB, BEAN> = ListAdapterHelper(this, layoutId, list)

    /**
     * 资源id已经不是必须的了
     *
     *
     * 无资源id有2种解决方式（任选其一）：
     * 1.什么都不做，根据泛型自动获取，但Proguard不能混淆[ViewBinding]的子类
     * 2.覆盖[.onCreateListViewHolder]，自己自定义即可
     */
    @JvmOverloads
    constructor(list: List<BEAN>? = null) : this(0, list) {
    }

    override fun getItemCount(): Int {
        return headerViewCount + footerViewCount + listSize()
    }

    override fun onCreateViewHolder2(
        parent: ViewGroup,
        @AdapterListType viewType: Int
    ): BaseViewHolder<*> {
        return listHelper.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder2(holder: BaseViewHolder<*>, position: Int) {
        listHelper.onBindViewHolder(holder, position)
    }

    @AdapterListType
    override fun getItemViewType(position: Int): Int {
        return listHelper.getItemViewType(position)
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
        return listHelper.onCreateDefaultViewHolder(parent, this)
    }

    interface OnAdapterBindListener<DB : ViewBinding, BEANS> {
        /**
         * 当viewHolder创建完成后
         */
        fun onViewHolderCreated(
            adapter: BaseListAdapter<DB, BEANS>?,
            holder: BaseViewHolder<DB>?
        ) {
        }

        fun onBindViewHolder(
            adapter: BaseListAdapter<DB, BEANS>?,
            holder: BaseViewHolder<DB>?,
            listPosition: Int,
            bean: BEANS
        )
    }

    override val list get() = listHelper.list

    override var headerView: View?
        get() = listHelper.headerView
        set(value) {
            listHelper.headerView = value
        }
    override var footerView: View?
        get() = listHelper.footerView
        set(value) {
            listHelper.footerView = value
        }

    companion object {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 追加一个懒汉写法
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * list+资源id+回调
         *
         * @param layoutId 资源id，必须有
         */
        fun <DB : ViewBinding, BEAN> createAdapter(
            @LayoutRes layoutId: Int,
            list: List<BEAN>? = null,
            listener: OnAdapterBindListener<DB, BEAN>? = null
        ): BaseListAdapter<DB, BEAN> {
            return object : BaseListAdapter<DB, BEAN>(layoutId, list) {
                override fun onCreateListViewHolder(parent: ViewGroup): BaseViewHolder<DB> {
                    val holder = super.onCreateListViewHolder(parent)
                    listener?.onViewHolderCreated(this, holder)
                    return holder
                }

                override fun onBindListViewHolder(
                    holder: BaseViewHolder<DB>,
                    listPosition: Int,
                    bean: BEAN
                ) {
                    listener?.onBindViewHolder(this, holder, listPosition, bean)
                }
            }
        }
    }

}