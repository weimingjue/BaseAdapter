package com.wang.adapters.container.item

import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.collection.ArraySet
import com.wang.adapters.container.BaseContainerAdapter
import com.wang.adapters.container.bean.IContainerBean
import com.wang.adapters.container.observer.IContainerObserver
import com.wang.adapters.helper.ListAdapterHelper.Companion.TYPE_MAX
import com.wang.adapters.helper.ListAdapterHelper.Companion.TYPE_MIN
import com.wang.adapters.holder.BaseViewHolder

/**
 * 和普通adapter操作一样，加了个currentBean来确定当前adapter的数据
 * 所有的position均为相对position
 * 获取adapter在整个RecyclerView的绝对position见[getCurrentPositionInfo]或[BaseViewHolder.commonPosition]、[BaseViewHolder.listPosition]
 * 简单的只有一个条目见[OneContainerItemAdapter]
 */
abstract class BaseContainerItemAdapter<BEAN : IContainerBean> {
    private val observers = ArraySet<IContainerObserver>()

    private var _containerAdapter: BaseContainerAdapter<*>? = null

    /**
     * observe主要用于notify
     * 此方法一般由父容器调用，所以不能加泛型
     */
    open fun registerDataSetObserver(observer: IContainerObserver) {
        observers.add(observer)
    }

    open fun unregisterDataSetObserver(observer: IContainerObserver) {
        observers.remove(observer)
    }

    /**
     * 刷新全部的adapter数据,其他方法均是局部刷新
     */
    open fun notifyDataSetChanged() {
        observers.forEach { it.notifyDataSetChanged() }
    }

    /**
     * @param relativePosition 就是item的position（我自己会计算绝对位置）
     * @param bean     list的bean数据,没有bean的话无法确定位置
     */
    open fun notifyItemChanged(relativePosition: Int, bean: BEAN) {
        notifyItemChanged(relativePosition, 1, bean)
    }

    open fun notifyItemChanged(relativePositionStart: Int, itemCount: Int, bean: BEAN) {
        observers.forEach { it.notifyItemChanged(relativePositionStart, itemCount, bean) }
    }

    open fun notifyItemInserted(relativePosition: Int, bean: BEAN) {
        notifyItemInserted(relativePosition, 1, bean)
    }

    open fun notifyItemInserted(relativePositionStart: Int, itemCount: Int, bean: BEAN) {
        observers.forEach { it.notifyItemInserted(relativePositionStart, itemCount, bean) }
    }

    open fun notifyItemMoved(relativeFromPosition: Int, relativeToPosition: Int, bean: BEAN) {
        observers.forEach { it.notifyItemMoved(relativeFromPosition, relativeToPosition, bean) }
    }

    open fun notifyItemRemoved(relativePosition: Int, bean: BEAN) {
        notifyItemRemoved(relativePosition, 1, bean)
    }

    open fun notifyItemRemoved(relativePositionStart: Int, itemCount: Int, bean: BEAN) {
        observers.forEach { it.notifyItemRemoved(relativePositionStart, itemCount, bean) }
    }

    /**
     * 将容器自己传进来（会在[BaseContainerAdapter.addAdapter]立即调用,正常使用不会为null）
     */
    open fun attachContainer(containerAdapter: BaseContainerAdapter<*>) {
        _containerAdapter = containerAdapter
    }

    /**
     * <*>或者<out x>调用方法时泛型居然是Nothing，实属醉了
     */
    internal fun castSuperAdapter() =
        this as BaseContainerItemAdapter<in IContainerBean>
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是经常用到或重写的方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 当前position在父adapter的附加信息
     *
     * 使用场景示例：有header展示线条，没header去掉线条；第一条展示红色，最后一条展示黑色
     */
    open fun getCurrentPositionInfo(
        bean: IContainerBean,
        itemAdapterPosition: Int
    ) = containerAdapter.getItemAdapterPositionInfo(bean, itemAdapterPosition)

    /**
     * 返回容器（会在[BaseContainerAdapter.addAdapter]立即调用,正常使用不会为null）
     */
    open val containerAdapter: BaseContainerAdapter<*>
        get() = _containerAdapter ?: throw NullPointerException("只有在addAdapter后才可调用")

    open fun getSpanSize(currentBean: BEAN, relativePosition: Int) = 1

    /**
     * @param relativePosition 相对的position
     * @return 不能超出范围, 超出就会被当成其他adapter的type
     *         当超出范围时会显式抛出异常
     */
    @IntRange(
        from = TYPE_MIN.toLong(),
        to = TYPE_MAX.toLong()
    )
    open fun getItemViewType(currentBean: BEAN, relativePosition: Int) = 0

    abstract fun getItemCount(currentBean: BEAN): Int

    /**
     * @param viewType 该adapter自己的type
     */
    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>

    /**
     * 想取绝对position见[BaseContainerAdapter.getAbsPosition]或[BaseViewHolder.commonPosition]、[BaseViewHolder.listPosition]
     *
     * @param relativePosition 属于该adapter的position
     * 如：[getItemCount]=1(每个bean只对应一条数据)，这个position一直是0（就是没用的意思）
     * 如：[getItemCount]=xx(你的bean里面还有自己的list)，这个position就是相对的值
     */
    abstract fun onBindViewHolder(
        holder: BaseViewHolder<*>,
        currentBean: BEAN,
        relativePosition: Int,
    )
}