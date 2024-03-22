package com.wang.adapters.container.item

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.wang.adapters.container.bean.IContainerBean
import com.wang.adapters.holder.BaseViewHolder
import com.wang.adapters.utils.ViewBindingHelper
import com.wang.adapters.utils.layoutInflater

/**
 * 一个list的item仅对应一条数据，如：聊天
 *
 * 无资源id有2种解决方式（任选其一）：
 * 1.什么都不做，根据泛型自动获取，但Proguard不能混淆[ViewBinding]的子类：
 * -keep class * extends androidx.databinding.ViewBinding
 * 2.覆盖[onCreateChildViewHolder]，自己自定义即可
 *
 * 暂时不支持id，后续添加
 */
@Suppress("UNCHECKED_CAST")
abstract class OneContainerItemAdapter<VB : ViewBinding, BEAN : IContainerBean> :
    BaseContainerItemAdapter<BEAN>() {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return onCreateChildViewHolder(parent)
    }

    final override fun onBindViewHolder(
        holder: BaseViewHolder<*>,
        currentBean: BEAN,
        relativePosition: Int
    ) {
        onBindChildViewHolder(holder as BaseViewHolder<VB>, currentBean)
    }

    /**
     * 仅一条数据，不允许重写
     */
    final override fun getItemViewType(currentBean: BEAN, relativePosition: Int) = 0
    final override fun getItemCount(currentBean: BEAN) = 1

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    open fun getCurrentPositionInfo(bean: IContainerBean) =
        getCurrentPositionInfo(bean, 0)

    protected open fun onCreateChildViewHolder(parent: ViewGroup): BaseViewHolder<VB> {
        return BaseViewHolder(
            ViewBindingHelper.getViewBindingInstance<VB>(this, parent.layoutInflater, parent)
        )
    }

    /**
     * 当然还有[getCurrentPositionInfo]
     */
    protected abstract fun onBindChildViewHolder(holder: BaseViewHolder<VB>, currentBean: BEAN)
}