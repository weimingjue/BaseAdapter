package com.wang.adapters.helper

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.interfaces.IListAdapter
import com.wang.adapters.utils.MATCH_PARENT
import com.wang.adapters.utils.ViewBindingHelper
import com.wang.adapters.utils.WRAP_CONTENT
import com.wang.adapters.utils.getDefLayoutParams
import com.wang.adapters.utils.layoutInflater

open class ListAdapterHelper<BEAN : Any>(
    val adapter: IListAdapter<BEAN>,
    list: List<BEAN>?
) {
    val list: MutableList<BEAN> = if (list == null) ArrayList() else ArrayList(list)

    fun onCreateHeaderFooterViewHolder(parent: ViewGroup): BaseViewHolder<*> {
        val fl = FrameLayout(parent.context)
        fl.layoutParams = parent.getDefLayoutParams().apply { height = MATCH_PARENT }
        return BaseViewHolder<ViewBinding>(fl)
    }

    fun onBindHeaderFooterViewHolder(holder: BaseViewHolder<*>, headerOrFooterView: View) {
        val fl = holder.itemView as FrameLayout
        val oldParent = headerOrFooterView.parent as? ViewGroup
        if (oldParent != fl) {
            oldParent?.removeView(headerOrFooterView)
            fl.removeAllViews()
            syncParamsToChild(fl, headerOrFooterView)
            fl.addView(headerOrFooterView)
        }
    }

    /**
     * 将fl的宽高和child同步
     */
    private fun syncParamsToChild(
        fl: FrameLayout,
        childView: View
    ) {
        val flParams = fl.layoutParams
        val childParams = childView.layoutParams
        if (flParams != null && childParams != null) {
            flParams.width = childParams.width
            flParams.height = childParams.height
        }
    }

    var headerView: View? = null
        set(value) {
            val oldHeaderView = field //旧view
            field = value
            if (field?.layoutParams == null) {
                field?.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }

            //4种情况
            if (value == null && oldHeaderView != null) {
                if (adapter is RecyclerView.Adapter<*>) {
                    adapter.notifyItemRemoved(0)
                } else {
                    adapter.notifyDataSetChanged()
                }
            } else if (value != null && oldHeaderView == null) {
                if (adapter is RecyclerView.Adapter<*>) {
                    adapter.notifyItemInserted(0)
                } else {
                    adapter.notifyDataSetChanged()
                }
            } else if (value !== oldHeaderView) {
                if (adapter is RecyclerView.Adapter<*>) {
                    adapter.notifyItemChanged(0)
                } else {
                    adapter.notifyDataSetChanged()
                }
            } //else相等忽略
        }

    var footerView: View? = null
        set(value) {
            val oldFooterView = field //旧view
            field = value
            if (field?.layoutParams == null) {
                field?.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }

            //4种情况
            if (value == null && oldFooterView != null) {
                if (adapter is RecyclerView.Adapter<*>) {
                    adapter.notifyItemRemoved(adapter.getItemCount()) //count已经减一了，所以不用减了
                } else {
                    adapter.notifyDataSetChanged()
                }
            } else if (value != null && oldFooterView == null) {
                if (adapter is RecyclerView.Adapter<*>) {
                    adapter.notifyItemInserted(adapter.getItemCount() - 1) //count已经加一了，所以需要减掉
                } else {
                    adapter.notifyDataSetChanged()
                }
            } else if (value !== oldFooterView) {
                if (adapter is RecyclerView.Adapter<*>) {
                    adapter.notifyItemChanged(adapter.getItemCount() - 1) //count不变
                } else {
                    adapter.notifyDataSetChanged()
                }
            } //else相等忽略
        }

    @IntDef(TYPE_BODY, TYPE_HEADER, TYPE_FOOTER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class AdapterListType  //该变量只能传入上面几种,否则会报错

    @AdapterListType
    fun getItemViewType(position: Int): Int {
        if (adapter.hasHeaderView && position == 0) {
            return TYPE_HEADER
        }
        if (adapter.hasFooterView && adapter.getItemCount() == position + 1) {
            return TYPE_FOOTER
        }
        return TYPE_BODY
    }

    fun <VB : ViewBinding> onCreateDefaultViewHolder(parent: ViewGroup, obj: Any) =
        BaseViewHolder(ViewBindingHelper.getViewBindingInstance<VB>(obj, parent.layoutInflater, parent))

    companion object {
        const val TYPE_MAX = 100_000
        const val TYPE_MIN = -100_000
        const val TYPE_MINUS = TYPE_MAX - TYPE_MIN

        const val TYPE_HEADER = TYPE_MIN - 1
        const val TYPE_FOOTER = TYPE_MIN - 2
        const val TYPE_BODY = 0

    }
}