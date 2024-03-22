package com.wang.adapters.interfaces

import kotlin.math.min

/**
 * 所有list的adapter的接口
 */
interface IListAdapter<BEAN : Any> : IAdapter {
    val list: MutableList<BEAN>

    /**
     * 获取指定bean
     *
     * @throws IndexOutOfBoundsException 不用多说吧
     */
    fun getItemData(listPosition: Int): BEAN {
        return list[listPosition]
    }

    fun getItemDataOrNull(listPosition: Int): BEAN? {
        return list.getOrNull(listPosition)
    }

    /**
     * 清空list,不刷新adapter
     */
    fun clearList() {
        list.clear()
    }

    /**
     * 添加全部条目,不刷新adapter，[addAllListAndNotify]
     */
    fun addAllList(addList: Collection<BEAN>?) {
        if (addList != null && list !== addList) {
            list.addAll(addList)
        }
    }

    fun listSize(): Int {
        return list.size
    }

    /**
     * list是否为空
     */
    fun isEmptyList(): Boolean {
        return list.isEmpty()
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // notify相关方法
    /////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 这个只是方便判断header、footer预留的属性，默认为0和false，由[IHeaderFooterListAdapter]实现
     */
    val headerViewCount get() = 0
    val hasHeaderView get() = false
    val footerViewCount get() = 0
    val hasFooterView get() = false

    /**
     * 刷新全部数据
     * @param newList 新的list
     */
    fun notifyDataSetChanged(newList: List<BEAN>?) {
        if (newList !== list) { //同一个对象当然啥都不需要干了
            list.clear()
            if (newList != null) {
                list.addAll(newList)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * 刷新list的position，解决[notifyItemChanged]的position问题
     */
    fun notifyListItemChanged(listPosition: Int) {
        if (listPosition < 0 || listPosition >= listSize()) {
            return
        }
        notifyItemChanged(listPosition + headerViewCount)
    }

    /**
     * 更新指定数据
     * @param newBean 被替换的数据
     * @param listPosition 被替换的位置
     */
    fun notifyListItemChanged(newBean: BEAN, listPosition: Int) {
        if (listPosition < 0 || listPosition >= listSize()) {
            return
        }
        list[listPosition] = newBean
        notifyListItemChanged(listPosition)
    }

    /**
     * 更新第一条数据
     */
    fun notifyListItemFirstChanged(newBean: BEAN) {
        notifyListItemChanged(newBean, 0)
    }

    /**
     * 更新最后一条数据
     */
    fun notifyListItemLastChanged(newBean: BEAN) {
        notifyListItemChanged(newBean, listSize() - 1)
    }

    fun notifyListItemRangeChanged(listPositionStart: Int, itemCount: Int) {
        if (listPositionStart < 0 || itemCount <= 0) {
            return
        }
        notifyItemRangeChanged(listPositionStart + headerViewCount, itemCount)
    }

    /**
     * @param newList 被替换的数据
     */
    fun notifyListItemRangeChanged(newList: List<BEAN>?, listPositionStart: Int) {
        if (newList.isNullOrEmpty() || listPositionStart < 0 || listPositionStart >= listSize()) {
            return
        }
        val outCount = newList.size + listPositionStart - listSize()

        when {
            outCount <= 0 -> {
                notifyListItemRangeChanged(listPositionStart, newList.size)
                newList.forEachIndexed { index, bean ->
                    list[listPositionStart + index] = bean
                }
            }

            else -> {
                //刷新的数据比以前多了，多的数据使用Insert
                val oldSize = listSize()
                list.subList(listPositionStart, listSize()).clear()
                list.addAll(newList)
                notifyListItemRangeChanged(listPositionStart, oldSize - listPositionStart)
                notifyItemRangeInserted(oldSize, outCount)
            }
        }
    }

    fun notifyListItemInserted(listPosition: Int) {
        if (listPosition < 0 || listPosition >= listSize()) {
            return
        }
        notifyItemInserted(listPosition + headerViewCount)
    }

    /**
     * 将数据插入到[list]并刷新
     * @param insertBean 要插入的数据
     */
    fun notifyListItemInserted(insertBean: BEAN, listPosition: Int) {
        if (listPosition < 0 || listPosition > listSize()) {
            return
        }
        list.add(listPosition, insertBean)
        notifyListItemInserted(listPosition)
    }

    fun notifyListItemFirstInserted(insertBean: BEAN) {
        notifyListItemInserted(insertBean, 0)
    }

    fun notifyListItemLastInserted(insertBean: BEAN) {
        notifyListItemInserted(insertBean, listSize())
    }

    fun notifyListItemRangeInserted(listPositionStart: Int, itemCount: Int) {
        if (listPositionStart < 0 || listPositionStart > listSize() || itemCount <= 0) {
            return
        }

        notifyItemRangeInserted(listPositionStart + headerViewCount, itemCount)
    }

    /**
     * 将数据插入到[list]的指定位置并刷新
     * @param insertedList 插入的数据
     * @param listPositionStart 插入数据起始位置，默认插入到列表最后
     * @param itemCount [insertedList]要插入多少条进去，默认全部插入
     */
    fun notifyListItemRangeInserted(insertedList: List<BEAN>?, listPositionStart: Int = listSize(), itemCount: Int = insertedList?.size ?: 0) {
        if (insertedList.isNullOrEmpty() || listPositionStart < 0 || listPositionStart > listSize() || itemCount <= 0) {
            return
        }
        when (listPositionStart) {
            0 -> {
                if (insertedList.size == 1) {
                    list.add(0, insertedList[0])
                } else {
                    val temp = insertedList + list
                    list.clear()
                    list.addAll(temp)
                }
            }

            listSize() -> {
                list.addAll(insertedList)
            }

            else -> {
                val t1 = list.subList(0, listPositionStart).toList()
                val t3 = list.subList(listPositionStart, listSize()).toList()
                list.clear()
                list.addAll(t1)
                list.addAll(insertedList)
                list.addAll(t3)
            }
        }
        notifyItemRangeInserted(listPositionStart + headerViewCount, itemCount)
    }

    /**
     * @param isMovedData 是否移动该数据
     *                false：你已经自己移动过了，这里只需要调用更新数据
     *                true：移动该条数据
     */
    fun notifyListItemMoved(listFromPosition: Int, listToPosition: Int, isMovedData: Boolean = false) {
        if (listFromPosition == listToPosition || listFromPosition < 0 || listFromPosition >= listSize() || listToPosition < 0 || listToPosition >= listSize()) {
            return
        }
        if (isMovedData) {
            val bean = list.removeAt(listFromPosition)
            list.add(listToPosition, bean)
        }
        notifyItemMoved(listFromPosition + headerViewCount, listToPosition + headerViewCount)
    }

    /**
     * @param isRemoData 是否删除该数据
     *                false：你已经自己删除过了，这里只需要调用更新数据
     *                true：删除该条数据
     */
    fun notifyListItemRemoved(listPosition: Int, isRemoData: Boolean = false) {
        if (listPosition < 0 || listPosition > listSize()) {
            return
        }
        if (isRemoData) {
            if (listPosition >= listSize()) {
                return
            }
            list.removeAt(listPosition)
        }
        notifyItemRemoved(listPosition + headerViewCount)
    }

    fun notifyListItemRangeRemoved(listPositionStart: Int, itemCount: Int, isRemoData: Boolean = false) {
        if (listPositionStart < 0 || listPositionStart > listSize() || itemCount <= 0) {
            return
        }
        if (isRemoData) {
            if (listPositionStart >= listSize()) {
                return
            }
            list.subList(listPositionStart, min(listSize(), listPositionStart + itemCount)).clear()
        }
        notifyItemRangeRemoved(listPositionStart + headerViewCount, itemCount)
    }
}