package com.wang.adapters.interfaces;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wang.container.interfaces.IAdapter;
import com.wang.container.interfaces.IItemClick;
import com.wang.container.interfaces.IListAdapter;

/**
 * 点击,长按,header,footer的回调
 */
public interface OnItemClickListener extends IItemClick {
    int POSITION_HEADER = -1, POSITION_FOOTER = -2;

    @CallSuper//一般不需要重写，所以加了此限制（如果真的不想调用super可以注解抑制掉错误）
    @Override
    @SuppressLint("MissingSuperCall")
    default void onClick(@NonNull View view) {
        IAdapter adapter = getAdapter(view);
        int formatPosition = getFormatPosition(adapter, getViewPosition(view));
        switch (formatPosition) {
            case POSITION_HEADER:
                onHeaderClick(view);
                break;
            case POSITION_FOOTER:
                onFooterClick(view);
                break;
            default:
                onItemClick(view, formatPosition);
                break;
        }
    }

    @CallSuper
    @Override
    @SuppressLint("MissingSuperCall")
    default boolean onLongClick(@NonNull View view) {
        IAdapter adapter = getAdapter(view);
        int formatPosition = getFormatPosition(adapter, getViewPosition(view));
        switch (formatPosition) {
            case POSITION_HEADER:
                return onHeaderLongClick(view);
            case POSITION_FOOTER:
                return onFooterLongClick(view);
            default:
                return onItemLongClick(view, formatPosition);
        }
    }

    /**
     * 获取当前view所在的position，注意header、footer
     */
    @CallSuper
    @Override
    default int getViewPosition(@NonNull View view) {
        return getViewHolder(view).getCommonPosition();
    }

    /**
     * 根据adapter和绝对position来获取格式化后的position
     *
     * @return {@link #POSITION_HEADER}{@link #POSITION_FOOTER}或者0-list.size
     */
    @CallSuper
    default int getFormatPosition(IAdapter adapter, int position) {
        if (adapter instanceof IListAdapter) {
            //listAdapter有header、footer事件
            IListAdapter listAdapter = (IListAdapter) adapter;
            if (listAdapter.getHeaderView() != null && position == 0) {
                return POSITION_HEADER;
            } else if (listAdapter.getFooterView() != null && listAdapter.getItemCount() == position + 1) {
                return POSITION_FOOTER;
            } else {
                if (listAdapter.getHeaderView() != null) {
                    position--;
                }
                return position;
            }
        } else {
            //普通adapter
            return position;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是item,header,footer的点击和长按回调
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * item被点击时
     *
     * @param listPosition list集合所对应的position,不需要-1
     */
    void onItemClick(@NonNull View view, int listPosition);

    /**
     * item被长按时
     *
     * @param listPosition list集合所对应的position,不需要-1
     */
    default boolean onItemLongClick(@NonNull View view, int listPosition) {
        return false;
    }

    /**
     * 添加的header被点击时,没有可以忽略
     */
    default void onHeaderClick(@NonNull View view) {
    }

    /**
     * 添加的header被长按时,没有可以忽略
     */
    default boolean onFooterLongClick(@NonNull View view) {
        return false;
    }

    /**
     * 添加的footer被点击时,没有可以忽略
     */
    default void onFooterClick(@NonNull View view) {
    }

    /**
     * 添加的footer被长按时,没有可以忽略
     */
    default boolean onHeaderLongClick(@NonNull View view) {
        return false;
    }
}