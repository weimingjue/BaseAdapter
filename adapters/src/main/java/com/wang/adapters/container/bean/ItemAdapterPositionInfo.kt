package com.wang.adapters.container.bean

import com.wang.adapters.container.item.BaseContainerItemAdapter

class ItemAdapterPositionInfo(
    absPosition: Int,
    containerListIndex: Int,
    itemPosition: Int,
    itemAdapter: BaseContainerItemAdapter<*>,
    hasHeader: Boolean,
    hasFooter: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
) {
    /**
     * 绝对值，container的list position
     */
    var absPosition = absPosition
        internal set

    /**
     * 绝对值，container的list position
     */
    var containerListIndex = containerListIndex
        internal set

    /**
     * 相对值，子adapter对应的相对position
     */
    var itemRelativePosition = itemPosition
        internal set

    var itemAdapter = itemAdapter
        internal set

    /**
     * 列表有没有header
     */
    var hasHeader = hasHeader
        internal set

    /**
     * 列表有没有footer
     */
    var hasFooter = hasFooter
        internal set

    /**
     * 是不是列表第一个（除了header）
     *
     *
     * 注意：整个adapter只有一个条目时既是第一个又是最后一个
     */
    var isFirst = isFirst
        internal set

    /**
     * 是不是列表里中间的（不是header、也不是footer）
     */
    val isCenter: Boolean
        get() = !(isFirst || isLast)

    /**
     * 是不是列表最后一个（除了footer）
     *
     *
     * 注意：整个adapter只有一个条目时既是第一个又是最后一个
     */
    var isLast = isLast
        internal set
}