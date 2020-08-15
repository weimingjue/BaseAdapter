package com.wang.adapters.interfaces;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.wang.adapters.R;
import com.wang.adapters.adapter.BaseAdapterRv;
import com.wang.container.holder.BaseViewHolder;
import com.wang.container.interfaces.IAdapter;
import com.zhy.view.flowlayout.FlowLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 高级功能:adapter套adapter的点击事件,具体用法见实现类
 * 方法回调见onParent...和onChild...，其他方法不要重写
 * <p>
 * create时请设置好adapter和layoutManager
 * bind里写法{@link BaseAdapterRv#setItemRvData}
 */
public interface OnItemItemClickListener extends OnItemClickListener {
    int ITEM_CLICK = 0, ITEM_LONG_CLICK = 1, ITEM_HEADER_CLICK = 2, ITEM_HEADER_LONG_CLICK = 3, ITEM_FOOTER_CLICK = 4, ITEM_FOOTER_LONG_CLICK = 5;

    @IntDef({ITEM_CLICK, ITEM_LONG_CLICK, ITEM_HEADER_CLICK, ITEM_HEADER_LONG_CLICK, ITEM_FOOTER_CLICK, ITEM_FOOTER_LONG_CLICK})
    @Retention(RetentionPolicy.SOURCE)
    @interface ClickType {
    }//该变量只能传入上面几种,否则会报错

    @Deprecated//不需要重写
    @Override
    default void onItemClick(@NonNull View view, int listPosition) {
        performClick(view, ITEM_CLICK, listPosition);
    }

    @Deprecated//不需要重写
    @Override
    default boolean onItemLongClick(@NonNull View view, int listPosition) {
        return performClick(view, ITEM_LONG_CLICK, listPosition);
    }

    @Deprecated//不需要重写
    @Override
    default void onHeaderClick(@NonNull View view) {
        performClick(view, ITEM_HEADER_CLICK, 0);
    }

    @Deprecated//不需要重写
    @Override
    default boolean onHeaderLongClick(@NonNull View view) {
        return performClick(view, ITEM_HEADER_LONG_CLICK, 0);
    }

    @Deprecated//不需要重写
    @Override
    default void onFooterClick(@NonNull View view) {
        performClick(view, ITEM_FOOTER_CLICK, 0);
    }

    @Deprecated//不需要重写
    @Override
    default boolean onFooterLongClick(@NonNull View view) {
        return performClick(view, ITEM_FOOTER_LONG_CLICK, 0);
    }

    /**
     * 总的点击分发
     */
    @Deprecated//不需要重写
    default boolean performClick(View view, @ClickType int clickType, int listPosition) {
        IAdapter parentAdapter = getParentAdapter(view);
        if (parentAdapter == null) {
            switch (clickType) {
                case ITEM_CLICK:
                    onParentItemClick(view, listPosition);
                    return true;
                case ITEM_LONG_CLICK:
                    return onParentItemLongClick(view, listPosition);
                case ITEM_HEADER_CLICK:
                    onParentHeaderClick(view);
                    return true;
                case ITEM_HEADER_LONG_CLICK:
                    return onParentHeaderLongClick(view);
                case ITEM_FOOTER_CLICK:
                    onParentFooterClick(view);
                    return true;
                case ITEM_FOOTER_LONG_CLICK:
                    return onParentFooterLongClick(view);
                default:
                    return false;
            }
        } else {
            int parentPosition = getParentViewHolder(view).getCommonPosition();
            int formatParentPosition = getFormatPosition(parentAdapter, parentPosition);
            switch (formatParentPosition) {
                case POSITION_HEADER:
                case POSITION_FOOTER:
                    return false;//item点击，暂不支持header、footer里的RecyclerView
                default:
                    switch (clickType) {
                        case ITEM_CLICK:
                            onChildItemClick(view, formatParentPosition, listPosition);
                            return true;
                        case ITEM_LONG_CLICK:
                            return onChildItemLongClick(view, formatParentPosition, listPosition);
                        case ITEM_HEADER_CLICK:
                            onChildHeaderClick(view, formatParentPosition);
                            return true;
                        case ITEM_HEADER_LONG_CLICK:
                            return onChildHeaderLongClick(view, formatParentPosition);
                        case ITEM_FOOTER_CLICK:
                            onChildFooterClick(view, formatParentPosition);
                            return true;
                        case ITEM_FOOTER_LONG_CLICK:
                            return onChildFooterLongClick(view, formatParentPosition);
                        default:
                            return false;
                    }
            }
        }
    }

    /**
     * 外层的position需要遍历
     */
    @Deprecated//不需要重写
    default IAdapter getParentAdapter(@NonNull View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            //第二层不建议使用ListView或GridView(肯定没有复用性,并且效率很差,可以尝试使用RecyclerView然后wrap)
//            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout || parent instanceof AdapterView) {
            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout) {
                return (IAdapter) ((ViewGroup) parent).getTag(R.id.tag_view_adapter);
            }
            parent = parent.getParent();
        }
        //没取到返回null
        return null;
    }

    /**
     * 获取当前view所在的ViewHolder
     */
    @Deprecated//不需要重写
    default BaseViewHolder getParentViewHolder(@NonNull View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            //第二层不建议使用ListView或GridView(肯定没有复用性,并且效率很差,可以尝试使用RecyclerView然后wrap)
//            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout || parent instanceof AdapterView) {
            if (parent instanceof RecyclerView || parent instanceof ViewPager || parent instanceof FlowLayout) {
                return (BaseViewHolder) ((ViewGroup) parent).getTag(R.id.tag_view_holder);
            }
            parent = parent.getParent();
        }
        //没取到返回null
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是parent的回调
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 当外层被点击时
     *
     * @param parentPosition 外层adapter的position
     */
    void onParentItemClick(@NonNull View view, int parentPosition);

    /**
     * 当外层被长按时
     *
     * @param parentPosition 外层adapter的position
     */
    default boolean onParentItemLongClick(@NonNull View view, int parentPosition) {
        return false;
    }

    default void onParentHeaderClick(@NonNull View view) {
    }

    default boolean onParentHeaderLongClick(@NonNull View view) {
        return false;
    }

    default void onParentFooterClick(@NonNull View view) {
    }

    default boolean onParentFooterLongClick(@NonNull View view) {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下是child的回调
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 当内层被点击时
     *
     * @param parentPosition 外层adapter对应的position
     * @param childPosition  内层adapter对应的position
     */
    void onChildItemClick(@NonNull View view, int parentPosition, int childPosition);

    /**
     * 当内层被长按时
     *
     * @param parentPosition 外层adapter对应的position
     * @param childPosition  内层adapter对应的position
     */
    default boolean onChildItemLongClick(@NonNull View view, int parentPosition, int childPosition) {
        return false;
    }

    default void onChildHeaderClick(@NonNull View view, int parentPosition) {
    }

    default boolean onChildHeaderLongClick(@NonNull View view, int parentPosition) {
        return false;
    }

    default void onChildFooterClick(@NonNull View view, int parentPosition) {
    }

    default boolean onChildFooterLongClick(@NonNull View view, int parentPosition) {
        return false;
    }
}