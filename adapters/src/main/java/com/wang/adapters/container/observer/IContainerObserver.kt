package com.wang.adapters.container.observer

import com.wang.adapters.container.bean.IContainerBean

/**
 * observer
 * 暂时不加泛型：加泛型太繁琐，父容器register还容易出错
 */
interface IContainerObserver {
    /**
     * 刷新全部的adapter数据,其他方法均是局部刷新
     */
    fun notifyDataSetChanged()

    /**
     * @param relativePosition 就是item的position（我自己会计算绝对位置）
     * @param bean     list的bean数据,没有bean的话无法确定位置
     */
    fun notifyItemChanged(relativePosition: Int, bean: IContainerBean) {
        notifyItemChanged(relativePosition, 1, bean)
    }

    fun notifyItemChanged(relativePositionStart: Int, itemCount: Int, bean: IContainerBean)

    fun notifyItemInserted(relativePosition: Int, bean: IContainerBean) {
        notifyItemInserted(relativePosition, 1, bean)
    }

    fun notifyItemInserted(relativePositionStart: Int, itemCount: Int, bean: IContainerBean)

    fun notifyItemMoved(
        relativeFromPosition: Int,
        relativePositionToPosition: Int,
        bean: IContainerBean
    )

    fun notifyItemRemoved(relativePosition: Int, bean: IContainerBean) {
        notifyItemRemoved(relativePosition, 1, bean)
    }

    fun notifyItemRemoved(relativePositionStart: Int, itemCount: Int, bean: IContainerBean)
}