package com.wang.adapters.adapter

import androidx.annotation.IntRange
import androidx.viewbinding.ViewBinding
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.utils.listPosition

/**
 * 轻量级可展开折叠的adapter
 *
 * 点击事件等想得到position见[getPositionInfo]
 *
 * 注意：目前仅为普通的2级数据展示
 * 暂未实现展开折叠（后续需要再添加）
 * 暂不支持header、footer、empty等功能（后续需要再添加）
 */
abstract class BaseExpandableAdapter<VB1 : ViewBinding, VB2 : ViewBinding, BEAN : Any>(vbc1: Class<VB1>, vbc2: Class<VB2>) :
    BaseMultiItemAdapter<BEAN>() {
    private val listRange get() = 0 until list.size

    init {
        addMultipleItem(object : OnMultipleListListener<VB1, BEAN> {
            override fun isThisType(adapter: BaseMultiItemAdapter<BEAN>, listPosition: Int, bean: BEAN) = getPositionInfo(listPosition).isParent
            override fun getViewBindingClass() = vbc1
            override fun onBindListViewHolder(adapter: BaseMultiItemAdapter<BEAN>, holder: BaseViewHolder<VB1>, bean: BEAN) =
                onBindParent(holder, bean, list.indexOf(bean))
        })
        addDefaultMultipleItem(
            object : OnMultipleListListener<VB2, BEAN> {
                override fun isThisType(adapter: BaseMultiItemAdapter<BEAN>, listPosition: Int, bean: BEAN) = true
                override fun getViewBindingClass(): Class<VB2> = vbc2
                override fun onBindListViewHolder(adapter: BaseMultiItemAdapter<BEAN>, holder: BaseViewHolder<VB2>, bean: BEAN) {
                    val listPosition = holder.listPosition
                    val info = getPositionInfo(listPosition)
                    onBindChild(holder, bean, info.childPosition)
                }
            })
    }

    final override fun getItemCount(): Int {
        var count = list.size
        (0 until list.size).forEach { parentPosition ->
            count += getChildCount(list[parentPosition], parentPosition)
        }
        return count
    }

    /**
     * 根据holder的position获取child真正的position
     * @param listPosition holder的listPosition
     */
    fun getPositionInfo(listPosition: Int): ExpandablePositionInfo {
        var itemStartPosition = 0
        listRange.forEach { parentPosition ->
            val childCount = getChildCount(list[parentPosition], parentPosition)
            val nextStartPosition = itemStartPosition + childCount + 1
            if (nextStartPosition > listPosition) {
                return ExpandablePositionInfo(
                    parentPosition,
                    listPosition - (itemStartPosition + 1),
                )
            } else {
                itemStartPosition = nextStartPosition
            }
        }
        throw IllegalArgumentException("没有找到对应的child你可能没有notify")
    }

    /**
     * 仅parent的
     */
    fun getAbsPosition(parentPosition: Int) = getAbsPosition(parentPosition, -1)

    /**
     * 根据相对position获取绝对position
     * @return adapter的position，可用于notify等操作
     */
    fun getAbsPosition(parentPosition: Int, childPosition: Int): Int {
        var position = 0
        (0..parentPosition).forEach { parentIndex ->
            position++
            position += if (parentIndex == parentPosition)
                childPosition
            else
                getChildCount(list[parentIndex], parentIndex)
        }
        return position
    }

    abstract fun getChildCount(parentItem: BEAN, parentPosition: Int): Int

    abstract fun onBindParent(
        holder: BaseViewHolder<VB1>,
        parentItem: BEAN,
        parentPosition: Int
    )

    abstract fun onBindChild(
        holder: BaseViewHolder<VB2>,
        parentItem: BEAN,
        childPosition: Int
    )

    class ExpandablePositionInfo(
        val parentPosition: Int,
        @IntRange(from = -1)
        val childPosition: Int,
    ) {
        /**
         * true：当前是父item，false：子item
         */
        val isParent = childPosition < 0
    }
}