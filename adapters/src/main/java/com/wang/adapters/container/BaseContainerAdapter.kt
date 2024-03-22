package com.wang.adapters.container

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.collection.SimpleArrayMap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.wang.adapters.adapter.BaseAdapter
import com.wang.adapters.container.bean.IContainerBean
import com.wang.adapters.container.bean.ItemAdapterPositionInfo
import com.wang.adapters.container.item.BaseContainerItemAdapter
import com.wang.adapters.container.item.OneContainerItemAdapter
import com.wang.adapters.container.observer.IContainerObserver
import com.wang.adapters.helper.ListAdapterHelper
import com.wang.adapters.helper.ListAdapterHelper.Companion.TYPE_FOOTER
import com.wang.adapters.helper.ListAdapterHelper.Companion.TYPE_HEADER
import com.wang.adapters.helper.ListAdapterHelper.Companion.TYPE_MAX
import com.wang.adapters.helper.ListAdapterHelper.Companion.TYPE_MIN
import com.wang.adapters.helper.ListAdapterHelper.Companion.TYPE_MINUS
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.interfaces.IHeaderFooterListAdapter
import com.wang.adapters.utils.createContainerAdapter
import kotlin.math.min

/**
 * 一个超级adapter可以添加其他adapter
 *
 *
 * 可以用在如：天猫首页、bilibili、今日头条、聊天列表页面
 *
 *
 * 核心思想：每个[BEAN]的item都当作一个adapter，所以再调用时都有个currentBean，adapter更新/判断数据时以currentBean为准
 *
 *
 * 使用前提（都是无关紧要的，但也要看看）：
 * 1.bean必须继承[IContainerBean]
 * 2.子adapter必须是[BaseContainerItemAdapter]、[OneContainerItemAdapter]的子类
 * 3.子adapter的type必须在[TYPE_MAX]、[TYPE_MIN]之间
 * 4.如果是GridLayoutManager必须在adapter前设置（在rv.setAdapter或[addAdapter]之前或手动调用[changedLayoutManager]）
 * 5.有header时直接调用BaseContainerAdapter的[notifyItemChanged]相关方法时需要+1（所有adapter的通病，建议使用[notifyListItemChanged]）（子adapter刷新时无需考虑父的header）
 * 其他限制暂时没发现
 *
 *
 * https://blog.csdn.net/weimingjue/article/details/106468916
 */
@SuppressLint("NotifyDataSetChanged")
class BaseContainerAdapter<BEAN : IContainerBean> @JvmOverloads constructor(list: List<BEAN>? = null) :
    BaseAdapter(), IHeaderFooterListAdapter<BEAN> {
    private var lastCachePositionInfo: ItemAdapterPositionInfo? = null
    private var internalLastCachePositionInfo: ItemAdapterPositionInfo? = null
    private val adaptersManager = MyAdaptersManager()
    private val childObservers: IContainerObserver = object : IContainerObserver {
        override fun notifyDataSetChanged() {
            this@BaseContainerAdapter.notifyDataSetChanged()
        }

        override fun notifyItemChanged(
            relativePositionStart: Int,
            itemCount: Int,
            bean: IContainerBean
        ) {
            val newPosition = getAbsPosition(bean, relativePositionStart)
            this@BaseContainerAdapter.notifyItemRangeChanged(newPosition, itemCount)
        }

        override fun notifyItemInserted(
            relativePositionStart: Int,
            itemCount: Int,
            bean: IContainerBean
        ) {
            val newPosition = getAbsPosition(bean, relativePositionStart)
            this@BaseContainerAdapter.notifyItemRangeInserted(newPosition, itemCount)
        }

        override fun notifyItemMoved(
            relativeFromPosition: Int,
            relativePositionToPosition: Int,
            bean: IContainerBean
        ) {
            val newPosition = getAbsPosition(bean, relativeFromPosition)
            this@BaseContainerAdapter.notifyItemMoved(
                newPosition,
                newPosition + (relativePositionToPosition - relativeFromPosition)
            )
        }

        override fun notifyItemRemoved(
            relativePositionStart: Int,
            itemCount: Int,
            bean: IContainerBean
        ) {
            val newPosition = getAbsPosition(bean, relativePositionStart)
            this@BaseContainerAdapter.notifyItemRangeRemoved(newPosition, itemCount)
        }
    }
    private var recyclerView: RecyclerView? = null
    private var lastLayoutManager: GridLayoutManager? = null

    /**
     * list相关代码合并
     */
    private val listHelper = ListAdapterHelper(this, list)

    init {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                onAdapterChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                onAdapterChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                onAdapterChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                onAdapterChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                onAdapterChanged()
            }

            fun onAdapterChanged() {
                lastCachePositionInfo = null
                internalLastCachePositionInfo?.absPosition = -999
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_HEADER, TYPE_FOOTER -> {
                listHelper.onCreateHeaderFooterViewHolder(parent)
            }

            else -> {
                adaptersManager.getAdapter(viewType / TYPE_MINUS)
                    .onCreateViewHolder(parent, viewType % TYPE_MINUS - TYPE_MAX)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                listHelper.onBindHeaderFooterViewHolder(holder, listHelper.headerView!!)
            }

            TYPE_FOOTER -> {
                listHelper.onBindHeaderFooterViewHolder(holder, listHelper.footerView!!)
            }

            else -> {
                val info = getCacheItemPositionInfo(position, true)
                val itemAdapter = info.itemAdapter.castSuperAdapter()
                itemAdapter.onBindViewHolder(
                    holder,
                    list[info.containerListIndex],
                    info.itemRelativePosition
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (hasHeaderView && position == 0) {
            return TYPE_HEADER
        }
        if (hasFooterView && itemCount == position + 1) {
            return TYPE_FOOTER
        }
        val info = getCacheItemPositionInfo(position, true)
        val itemAdapter = info.itemAdapter.castSuperAdapter()
        val itemType =
            itemAdapter.getItemViewType(list[info.containerListIndex], info.itemRelativePosition)
        if (itemType <= TYPE_MIN || itemType >= TYPE_MAX) {
            throw RuntimeException("你adapter（" + itemAdapter.javaClass + "）的type必须在" + TYPE_MIN + "~" + TYPE_MAX + "之间，type：" + itemType)
        }
        //根据mItemAdapters的position返回type，取的时候比较方便
        //此处返回的type>0
        return adaptersManager.getPosition(itemAdapter.javaClass) * TYPE_MINUS + itemType + TYPE_MAX
    }

    override fun getItemCount(): Int {
        var count = headerViewCount + footerViewCount
        listHelper.list.forEach { bean ->
            val itemAdapter =
                adaptersManager.getAdapter(bean.getBindAdapterClass()).castSuperAdapter()
            count += itemAdapter.getItemCount(bean)
        }
        return count
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        checkLayoutManager()
    }

    private fun checkLayoutManager() {
        (recyclerView?.layoutManager as? GridLayoutManager)?.let {
            if (lastLayoutManager != it) {
                changedLayoutManager(it)
            }
        }
    }

    /**
     * 根据绝对position获取子adapter的相关信息
     *
     * @param absPosition 绝对position
     * @param internalRecycle 开启内部循环利用，返回值绝对不可声明final（当然为了安全，外部调用默认false）
     */
    internal fun getCacheItemPositionInfo(
        absPosition: Int,
        internalRecycle: Boolean
    ): ItemAdapterPositionInfo {
        //取缓存
        val cache = if (internalRecycle) internalLastCachePositionInfo else lastCachePositionInfo
        if (cache?.absPosition == absPosition) {
            return cache
        }

        val containerListIndex = absPosition - headerViewCount

        //itemAdapter的position=0时的真实位置
        var itemStartPosition = 0
        listHelper.list.forEachIndexed { i, bean ->
            val itemAdapter =
                adaptersManager.getAdapter(bean.getBindAdapterClass()).castSuperAdapter()
            val itemCount = itemAdapter.getItemCount(bean)
            val nextStartPosition = itemStartPosition + itemCount

            if (nextStartPosition > containerListIndex) {
                //下一个adapter的位置比position大说明当前type就在这个adapter中

                val itemPosition = containerListIndex - itemStartPosition

                //当前状态
                val isFirst = containerListIndex == 0
                val isLast = containerListIndex == listHelper.list.lastIndex

                if (internalRecycle) {
                    //内部使用则复用单独一个对象
                    val info = internalLastCachePositionInfo?.also {
                        it.absPosition = absPosition
                        it.containerListIndex = i
                        it.itemRelativePosition = itemPosition
                        it.itemAdapter = itemAdapter
                        it.hasHeader = hasHeaderView
                        it.hasFooter = hasFooterView
                        it.isFirst = isFirst
                        it.isLast = isLast
                    } ?: ItemAdapterPositionInfo(
                        absPosition = absPosition,
                        containerListIndex = i,
                        itemPosition = itemPosition,
                        itemAdapter = itemAdapter,
                        hasHeader = hasHeaderView,
                        hasFooter = hasFooterView,
                        isFirst = isFirst,
                        isLast = isLast
                    )
                    internalLastCachePositionInfo = info
                    return info
                }
                val info = ItemAdapterPositionInfo(
                    absPosition = absPosition,
                    containerListIndex = i,
                    itemPosition = itemPosition,
                    itemAdapter = itemAdapter,
                    hasHeader = hasHeaderView,
                    hasFooter = hasFooterView,
                    isFirst = isFirst,
                    isLast = isLast
                )
                lastCachePositionInfo = info
                return info
            } else {
                //循环相加
                itemStartPosition = nextStartPosition
            }
        }
        throw RuntimeException("没有取到对应的type,可能你没有(及时)刷新adapter")
    }

    /**
     * position、adapter、class唯一并且可以互相取值
     */
    private inner class MyAdaptersManager {
        val map = SimpleArrayMap<Class<out BaseContainerItemAdapter<*>>, Int>(8)
        val list = ArrayList<BaseContainerItemAdapter<*>>(8)

        fun addAdapter(adapters: List<BaseContainerItemAdapter<*>>) {
            adapters.forEach { adapter ->
                adapter.attachContainer(this@BaseContainerAdapter)
                adapter.registerDataSetObserver(childObservers)
                if (!map.containsKey(adapter.javaClass)) {
                    list.add(adapter)
                    map.put(adapter.javaClass, list.lastIndex)
                }
            }
        }

        fun getAdapter(position: Int): BaseContainerItemAdapter<*> {
            return list.getOrNull(position)
                ?: throw RuntimeException("缺少对应的adapter，adapter数量：" + list.size + "，当前index：" + position)
        }

        fun getAdapter(cls: Class<out BaseContainerItemAdapter<*>>): BaseContainerItemAdapter<*> {
            val index = map[cls] ?: throw RuntimeException("缺少对应的adapter：$cls")
            return list[index]
        }

        fun getPosition(cls: Class<out BaseContainerItemAdapter<*>>): Int {
            return map[cls] ?: throw NullPointerException("一般是数据变化没有(及时)刷新adapter导致的")
        }

        fun remove(position: Int) {
            list.removeAt(position).also { map.remove(it.javaClass) }
        }

        fun remove(cls: Class<out BaseContainerItemAdapter<*>>?) {
            map.remove(cls)?.also { list.removeAt(it) }
        }

        fun clear() {
            list.clear()
            map.clear()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是可调用方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 添加adapter.重复则不会被添加,必须先删除
     * 当然可以预先添加用不到的adapter
     * [createContainerAdapter]、[createOneItemAdapter]
     */
    fun addAdapter(vararg adapters: BaseContainerItemAdapter<*>) {
        addAdapter(adapters.toList())
    }

    /**
     * 添加adapter.重复则不会被添加,必须先删除
     * 当然可以预先添加用不到的adapter
     * [createContainerAdapter]、[createOneItemAdapter]
     */
    fun addAdapter(adapters: List<BaseContainerItemAdapter<*>>) {
        adaptersManager.addAdapter(adapters)
        checkLayoutManager()
        notifyDataSetChanged()
    }

    /**
     * 删除指定adapter
     *
     * @param adapterPosition 按添加顺序第几个
     */
    fun removeAdapter(adapterPosition: Int) {
        adaptersManager.remove(adapterPosition)
        notifyDataSetChanged()
    }

    /**
     * 删除指定adapter
     *
     * @param adapterClass 哪个adapter
     */
    fun removeAdapter(adapterClass: Class<out BaseContainerItemAdapter<*>>?) {
        adaptersManager.remove(adapterClass)
        notifyDataSetChanged()
    }

    /**
     * 清空adapter
     */
    fun removeAllAdapter() {
        adaptersManager.clear()
        notifyDataSetChanged()
    }

    /**
     * 返回所有的adapter
     */
    fun getAdapters(): List<BaseContainerItemAdapter<*>> {
        //为了安全起见，不允许私自增删
        return adaptersManager.list.toList()
    }

    override val list get() = listHelper.list

    /**
     * 根据bean对象和adapter的相对位置获取绝对位置
     *
     * @param relativePosition 相对potion
     */
    fun getAbsPosition(bean: IContainerBean, relativePosition: Int): Int {
        var position = relativePosition
        listHelper.list.forEach { listBean ->
            if (listBean === bean) {
                return position + headerViewCount
            } else {
                val itemAdapter =
                    adaptersManager.getAdapter(listBean.getBindAdapterClass()).castSuperAdapter()
                position += itemAdapter.getItemCount(listBean)
            }
        }
        throw RuntimeException("在list中没有找到传入的bean对象$bean")
    }

    /**
     * 根据绝对position获取对应adapter的额外信息
     *
     * @param absPosition 一般为[BaseViewHolder.commonPosition]
     * @return 不建议声明为final，因为[notifyItemChanged]相关方法时并不会更新里面的position
     */
    @MainThread
    fun getItemAdapterPositionInfo(absPosition: Int): ItemAdapterPositionInfo {
        return getCacheItemPositionInfo(absPosition, false)
    }

    /**
     * 根据bean和相对position获取对应adapter的额外信息
     *
     * @param relativePosition 相对potion
     * @return 不建议声明为final，因为[notifyItemChanged]相关方法时并不会更新里面的position
     */
    @MainThread
    fun getItemAdapterPositionInfo(
        bean: IContainerBean,
        relativePosition: Int
    ): ItemAdapterPositionInfo {
        return getItemAdapterPositionInfo(getAbsPosition(bean, relativePosition))
    }

    /**
     * 把rv的LayoutManager改成其他的GridLayoutManager时，此方法理论上没啥用
     */
    fun changedLayoutManager(manager: GridLayoutManager) {
        lastLayoutManager = manager
        manager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (hasHeaderView && position == 0) {
                    return manager.spanCount
                } else if (hasFooterView && itemCount == position + 1) {
                    return manager.spanCount
                }
                val info = getCacheItemPositionInfo(position, true)
                val itemAdapter = info.itemAdapter.castSuperAdapter()
                return itemAdapter.getSpanSize(
                    list[info.containerListIndex],
                    info.itemRelativePosition
                )
            }
        }
    }

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

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // container不支持list的范围刷新效果（很难算出），此处重载为全部刷新
    /////////////////////////////////////////////////////////////////////////////////////////////////
    override fun notifyListItemChanged(listPosition: Int) {
        if (listPosition < 0 || listPosition >= listSize()) {
            return
        }
        notifyDataSetChanged()
    }

    override fun notifyListItemRangeChanged(listPositionStart: Int, itemCount: Int) {
        if (listPositionStart < 0 || itemCount <= 0) {
            return
        }
        notifyDataSetChanged()
    }

    override fun notifyListItemInserted(listPosition: Int) {
        if (listPosition < 0 || listPosition >= listSize()) {
            return
        }
        notifyDataSetChanged()
    }

    override fun notifyListItemRangeInserted(listPositionStart: Int, itemCount: Int) {
        if (listPositionStart < 0 || listPositionStart > listSize() || itemCount <= 0) {
            return
        }
        notifyDataSetChanged()
    }

    override fun notifyListItemMoved(listFromPosition: Int, listToPosition: Int, isMovedData: Boolean) {
        if (listFromPosition == listToPosition || listFromPosition < 0 || listFromPosition >= listSize() || listToPosition < 0 || listToPosition >= listSize()) {
            return
        }
        if (isMovedData) {
            val bean = list.removeAt(listFromPosition)
            if (listFromPosition > listToPosition) {
                list.add(listToPosition, bean)
            } else {
                list.add(listToPosition - 1, bean)
            }
        }
        notifyDataSetChanged()
    }

    override fun notifyListItemRemoved(listPosition: Int, isRemoData: Boolean) {
        if (listPosition < 0) {
            return
        }
        if (isRemoData) {
            if (listPosition >= listSize()) {
                return
            }
            list.removeAt(listPosition)
        }
        notifyDataSetChanged()
    }

    override fun notifyListItemRangeRemoved(listPositionStart: Int, itemCount: Int, isRemoData: Boolean) {
        if (listPositionStart < 0 || itemCount <= 0) {
            return
        }
        if (isRemoData) {
            if (listPositionStart >= listSize()) {
                return
            }
            list.subList(listPositionStart, min(listSize(), listPositionStart + itemCount)).clear()
        }
        notifyDataSetChanged()
    }
}