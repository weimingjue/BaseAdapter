package com.wang.adapters.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.viewbinding.ViewBinding
import com.wang.adapters.helper.ListAdapterHelper
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.interfaces.IHeaderFooterListAdapter
import com.wang.adapters.utils.ViewBindingHelper
import com.wang.adapters.utils.forEachReverseSequence
import com.wang.adapters.utils.listPosition

/**
 * @作者 王能
 * api很简单，就2个：[addMultipleItem]、[addDefaultMultipleItem]
 */
@SuppressLint("NotifyDataSetChanged")
open class BaseMultiItemAdapter<BEAN : Any>(list: List<BEAN>? = null) : BaseAdapter(), IHeaderFooterListAdapter<BEAN> {
    private val listHelper = ListAdapterHelper(this, list)

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

    /**
     * 第0个是兜底策略，所以遍历集合都是倒序
     */
    private val idInfoList = ArrayList<OnMultipleListListener<out ViewBinding, BEAN>>(4)

    override fun getItemCount() = headerViewCount + footerViewCount + listSize()

    override fun getItemViewType(position: Int): Int {
        when (val type = listHelper.getItemViewType(position)) {
            ListAdapterHelper.TYPE_HEADER, ListAdapterHelper.TYPE_FOOTER -> return type
            else -> {
                val listPosition = position - headerViewCount
                idInfoList.forEachReverseSequence { index, listener ->
                    if (listener.isThisType(this, listPosition, list[listPosition])) {
                        return index
                    }
                }
                throw IllegalArgumentException("第${listPosition}个没有对应的type或没有兜底处理")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        ListAdapterHelper.TYPE_HEADER, ListAdapterHelper.TYPE_FOOTER -> {
            listHelper.onCreateHeaderFooterViewHolder(parent)
        }
        //TYPE_BODY
        else -> {
            val clazz = idInfoList[viewType].getViewBindingClass()
            val vb = ViewBindingHelper.getViewBindingInstanceByClass(
                clazz,
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            BaseViewHolder(vb)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (getItemViewType(position)) {
            ListAdapterHelper.TYPE_HEADER -> {
                listHelper.onBindHeaderFooterViewHolder(holder, headerView!!)
            }

            ListAdapterHelper.TYPE_FOOTER -> {
                listHelper.onBindHeaderFooterViewHolder(holder, footerView!!)
            }

            else -> {//TYPE_BODY
                idInfoList.forEachReverseSequence { _, listener ->
                    val listPosition = holder.listPosition
                    if (listener.isThisType(this, listPosition, list[listPosition])) {
                        //kotlin bug，直接调用会提示必须传Nothing？？？
                        @Suppress("UNCHECKED_CAST")
                        bindVB(holder as BaseViewHolder<ViewBinding>, list[listPosition], listener)
                        return
                    }
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <VB : ViewBinding> bindVB(
        holder: BaseViewHolder<ViewBinding>,
        bean: BEAN,
        listener: OnMultipleListListener<VB, BEAN>
    ) {
        @Suppress("UNCHECKED_CAST")
        listener.onBindListViewHolder(
            adapter = this,
            holder = holder as BaseViewHolder<VB>,
            bean = bean
        )
    }

    /**
     * 多条目实现类
     */
    @Keep
    interface OnMultipleListListener<VB : ViewBinding, BEAN : Any> {
        /**
         * 这个bean是否是当前类型
         */
        fun isThisType(
            adapter: BaseMultiItemAdapter<BEAN>,
            listPosition: Int,
            bean: BEAN
        ): Boolean

        /**
         * 默认走反射
         */
        fun getViewBindingClass(): Class<VB> {
            return ViewBindingHelper.getViewBindingClass(this.javaClass)
                ?: throw IllegalArgumentException("没有找到类${this}的ViewBinding，请检查")
        }

        fun onBindListViewHolder(
            adapter: BaseMultiItemAdapter<BEAN>,
            holder: BaseViewHolder<VB>,
            bean: BEAN
        )
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 添加回调
     */
    fun <VB : ViewBinding> addMultipleItem(listener: OnMultipleListListener<VB, BEAN>) {
        idInfoList.add(listener)
        notifyDataSetChanged()
    }

    /**
     *inline版实现
     */
    inline fun <reified VB : ViewBinding> addMultipleItem(
        crossinline isThisTypeCallback: BaseMultiItemAdapter<BEAN>.(listPosition: Int, bean: BEAN) -> Boolean,
        crossinline onBindListViewHolderCallback: BaseMultiItemAdapter<BEAN>.(holder: BaseViewHolder<VB>, bean: BEAN) -> Unit,
    ) {
        addMultipleItem(createListener(isThisTypeCallback, onBindListViewHolderCallback))
    }

    /**
     * 带vb
     */
    inline fun <reified VB : ViewBinding> addMultipleItem(
        crossinline isThisTypeCallback: BaseMultiItemAdapter<BEAN>.(listPosition: Int, bean: BEAN) -> Boolean,
        crossinline onBindListViewHolderCallback: BaseMultiItemAdapter<BEAN>.(holder: BaseViewHolder<VB>, vb: VB, bean: BEAN) -> Unit,
    ) {
        addMultipleItem(isThisTypeCallback) { holder, bean -> this.onBindListViewHolderCallback(holder, holder.vb, bean) }
    }

    /**
     * 对于不同类型的bean，安全转为对应bean
     */
    inline fun <reified VB : ViewBinding, reified T : BEAN> addAsMultipleItem(
        crossinline onBindListViewHolderCallback: BaseMultiItemAdapter<BEAN>.(holder: BaseViewHolder<VB>, bean: T) -> Unit,
    ) {
        addMultipleItem(createListener<VB>({ _, bean -> bean is T }, { holder, bean -> this.onBindListViewHolderCallback(holder, bean as T) }))
    }

    /**
     * 带vb
     */
    inline fun <reified VB : ViewBinding, reified T : BEAN> addAsMultipleItem(
        crossinline onBindListViewHolderCallback: BaseMultiItemAdapter<BEAN>.(holder: BaseViewHolder<VB>, vb: VB, bean: T) -> Unit,
    ) {
        addAsMultipleItem<VB, T> { holder, bean -> this.onBindListViewHolderCallback(holder, holder.vb, bean) }
    }

    /**
     * 添加兜底type（else）
     * 注意[OnMultipleListListener.isThisType]请直接返回true，暂时不加限制了
     * 建议使用inline简化效果的重载方法
     */
    fun <VB : ViewBinding> addDefaultMultipleItem(listener: OnMultipleListListener<VB, BEAN>) {
        idInfoList.add(0, listener)
        notifyDataSetChanged()
    }

    /**
     * inline版
     */
    inline fun <reified VB : ViewBinding> addDefaultMultipleItem(
        crossinline onBindListViewHolderCallback: BaseMultiItemAdapter<BEAN>.(
            holder: BaseViewHolder<VB>,
            bean: BEAN
        ) -> Unit = { _, _ -> }
    ) {
        addDefaultMultipleItem(createListener({ _, _ -> true }, onBindListViewHolderCallback))
    }

    inline fun <reified VB : ViewBinding> createListener(
        crossinline isThisTypeCallback: BaseMultiItemAdapter<BEAN>.(listPosition: Int, bean: BEAN) -> Boolean,
        crossinline onBindListViewHolderCallback: BaseMultiItemAdapter<BEAN>.(holder: BaseViewHolder<VB>, bean: BEAN) -> Unit
    ) = object : OnMultipleListListener<VB, BEAN> {
        override fun isThisType(
            adapter: BaseMultiItemAdapter<BEAN>,
            listPosition: Int,
            bean: BEAN
        ): Boolean {
            return adapter.isThisTypeCallback(listPosition, bean)
        }

        override fun getViewBindingClass(): Class<VB> {
            return VB::class.java
        }

        override fun onBindListViewHolder(
            adapter: BaseMultiItemAdapter<BEAN>,
            holder: BaseViewHolder<VB>,
            bean: BEAN
        ) {
            adapter.onBindListViewHolderCallback(holder, bean)
        }
    }
}