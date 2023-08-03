package com.wang.adapters.adapter

import android.annotation.TargetApi
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.wang.adapters.adapter.BaseAdapterMultipleList.OnMultipleListListener
import com.wang.adapters.helper.ListAdapterHelper
import com.wang.adapters.interfaces.OnItemClickListener
import com.wang.container.holder.BaseViewHolder
import com.wang.container.interfaces.IListAdapter

/**
 * 简单的多条目单list，复杂布局请使用[com.wang.container.BaseContainerAdapter]
 *
 *
 * 带header、footer
 * 多条目就一个方法[.addMultipleItem]，入参回调[OnMultipleListListener]
 */
class BaseAdapterMultipleList<BEAN> : BaseAdapter(),
    IListAdapter<BEAN, ViewBinding, OnItemClickListener> {
    private val mIdInfoList = SparseArray<OnMultipleListListener<*, BEAN>>(4)
    private val mHelper: ListAdapterHelper<ViewBinding, BEAN> = ListAdapterHelper(this, 0, null)
    override fun getItemCount(): Int {
        return headerViewCount + footerViewCount + listSize()
    }

    override fun onCreateViewHolder2(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            ListAdapterHelper.TYPE_HEADER, ListAdapterHelper.TYPE_FOOTER -> mHelper.onCreateHeaderFooterViewHolder(
                parent
            )

            else -> {
                val index = -viewType
                mIdInfoList.valueAt(index)
                    .onCreateListViewHolder(this, parent, mIdInfoList.keyAt(index))
            }
        }
    }

    override fun onBindViewHolder2(holder: BaseViewHolder<*>, position: Int) {
        mHelper.onBindViewHolder(holder, position)
    }

    override fun getItemViewType(position: Int): Int {
        val type = mHelper.getItemViewType(position)
        return if (type == ListAdapterHelper.Companion.TYPE_BODY) {
            val listPosition = position - headerViewCount
            for (i in 0 until mIdInfoList.size()) {
                if (mIdInfoList.valueAt(i).isThisType(this, listPosition, list[listPosition])) {
                    return -i //-和header、footer错开
                }
            }
            throw RuntimeException("没有对应的type来接收position：$position")
        } else {
            type
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // list相关的方法，其他方法请使用getList进行操作
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return 注意list是否传了null或者根本没传
     */
    override fun getList(): List<BEAN> {
        return mHelper.mList
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是增加的方法
    ///////////////////////////////////////////////////////////////////////////
    @Deprecated("")
    @TargetApi(999)
    override fun onCreateListViewHolder(parent: ViewGroup): BaseViewHolder<ViewBinding> {
        throw RuntimeException("暂时无法实现，请勿调用")
    }

    override fun onBindListViewHolder(
        holder: BaseViewHolder<ViewBinding>,
        listPosition: Int,
        bean: BEAN
    ) {
        for (i in 0 until mIdInfoList.size()) {
            val listener: OnMultipleListListener<*, *> = mIdInfoList.valueAt(i)
            if (listener.isThisType(this, listPosition, list[listPosition])) {
                listener.onBindListViewHolder(this, holder, listPosition, bean)
            }
        }
    }

    /**
     * @param view null表示删除，view的parent为FrameLayout，默认match、wrap
     */
    override fun setHeaderView(view: View?) {
        mHelper.setHeaderView(view)
    }

    override fun getHeaderView(): View? {
        return mHelper.mHeaderView
    }

    /**
     * @param view null表示删除，view的parent为FrameLayout，默认match、wrap
     */
    override fun setFooterView(view: View?) {
        mHelper.setFooterView(view)
    }

    override fun getFooterView(): View? {
        return mHelper.mFooterView
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 添加多条目
     */
    fun <DB : ViewBinding?> addMultipleItem(
        layoutRes: Int,
        listener: OnMultipleListListener<DB, BEAN>
    ): BaseAdapterMultipleList<BEAN> {
        mIdInfoList.put(layoutRes, listener)
        notifyDataSetChanged()
        return this
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 多条目类
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 多条目实现
     */
    interface OnMultipleListListener<DB : ViewBinding?, BEANS> {
        /**
         * 这个bean是否是当前类型
         */
        fun isThisType(
            adapter: BaseAdapterMultipleList<BEANS>,
            listPosition: Int,
            bean: BEANS
        ): Boolean

        /**
         * 注释同[BaseAdapterList.onCreateListViewHolder]
         */
        fun onCreateListViewHolder(
            adapter: BaseAdapterMultipleList<BEANS>,
            parent: ViewGroup,
            @LayoutRes layoutId: Int
        ): BaseViewHolder<DB> {
            return BaseViewHolder(parent, layoutId)
        }

        /**
         * 注释同[BaseAdapterList.onBindListViewHolder]
         */
        fun onBindListViewHolder(
            adapter: BaseAdapterMultipleList<BEANS>,
            holder: BaseViewHolder<DB>,
            listPosition: Int,
            bean: BEANS
        ) {
        }
    }
}