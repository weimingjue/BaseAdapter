package com.wang.adapters.adapter;

import android.annotation.TargetApi;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import com.wang.adapters.helper.ListAdapterHelper;
import com.wang.adapters.interfaces.OnItemClickListener;
import com.wang.container.holder.BaseViewHolder;
import com.wang.container.interfaces.IListAdapter;

import java.util.List;

/**
 * 简单的多条目单list，复杂布局请使用{@link com.wang.container.BaseContainerAdapter}
 * <p>
 * 带header、footer
 * 多条目就一个方法{@link #addMultipleItem}，入参回调{@link OnMultipleListListener}
 */
public class BaseAdapterRvMultipleList<BEAN> extends BaseAdapterRv implements IListAdapter<BEAN, ViewDataBinding, OnItemClickListener> {
    private final SparseArray<OnMultipleListListener<?, BEAN>> mIdInfoList = new SparseArray<>(4);

    private final ListAdapterHelper<ViewDataBinding, BEAN> mHelper;

    public BaseAdapterRvMultipleList() {
        mHelper = new ListAdapterHelper<>(this, 0, null);
    }

    @Override
    public final int getItemCount() {
        return mHelper.getItemCount();
    }

    @NonNull
    @Override
    protected final BaseViewHolder onCreateViewHolder2(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ListAdapterHelper.TYPE_HEADER://每次create都会new一个布局，防止多次create导致崩溃
            case ListAdapterHelper.TYPE_FOOTER:
                return mHelper.onCreateHeaderFooterViewHolder(parent);
            default:
                int index = -viewType;
                return mIdInfoList.valueAt(index).onCreateListViewHolder(this, parent, mIdInfoList.keyAt(index));
        }
    }

    @Override
    protected final void onBindViewHolder2(@NonNull BaseViewHolder holder, int position) {
        mHelper.onBindViewHolder(holder, position);
    }

    @Override
    public final int getItemViewType(int position) {
        int type = mHelper.getItemViewType(position);
        if (type == ListAdapterHelper.TYPE_BODY) {
            int listPosition = mHelper.getListPosition(position);
            for (int i = 0; i < mIdInfoList.size(); i++) {
                if (mIdInfoList.valueAt(i).isThisType(this, listPosition, getList().get(listPosition))) {
                    return -i;//-和header、footer错开
                }
            }
            throw new RuntimeException("没有对应的type来接收position：" + position);
        } else {
            return type;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // list相关的方法，其他方法请使用getList进行操作
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return 注意list是否传了null或者根本没传
     */
    @NonNull
    @Override
    public List<BEAN> getList() {
        return mHelper.mList;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是增加的方法
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    @Deprecated
    @Override
    @TargetApi(999)
    public BaseViewHolder<ViewDataBinding> onCreateListViewHolder(@NonNull ViewGroup parent) {
        throw new RuntimeException("暂时无法实现，请勿调用");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindListViewHolder(@NonNull BaseViewHolder<ViewDataBinding> holder, int listPosition, BEAN bean) {
        for (int i = 0; i < mIdInfoList.size(); i++) {
            OnMultipleListListener listener = mIdInfoList.valueAt(i);
            if (listener.isThisType(this, listPosition, getList().get(listPosition))) {
                listener.onBindListViewHolder(this, holder, listPosition, bean);
            }
        }
    }

    /**
     * @param view null表示删除，view的parent为FrameLayout，默认match、wrap
     */
    public void setHeaderView(@Nullable View view) {
        mHelper.setHeaderView(view);
    }

    @Nullable
    @Override
    public View getHeaderView() {
        return mHelper.mHeaderView;
    }

    /**
     * @param view null表示删除，view的parent为FrameLayout，默认match、wrap
     */
    @Override
    public void setFooterView(@Nullable View view) {
        mHelper.setFooterView(view);
    }

    @Nullable
    @Override
    public View getFooterView() {
        return mHelper.mFooterView;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 公共方法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 添加多条目
     */
    public <DB extends ViewDataBinding> BaseAdapterRvMultipleList<BEAN> addMultipleItem(int layoutRes, @NonNull OnMultipleListListener<DB, BEAN> listener) {
        mIdInfoList.put(layoutRes, listener);
        notifyDataSetChanged();
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 多条目类
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 多条目实现
     */
    public interface OnMultipleListListener<DB extends ViewDataBinding, BEANS> {

        /**
         * 这个bean是否是当前类型
         */
        boolean isThisType(@NonNull BaseAdapterRvMultipleList<BEANS> adapter, int listPosition, @NonNull BEANS bean);

        /**
         * 注释同{@link BaseAdapterRvList#onCreateListViewHolder}
         */
        @NonNull
        default BaseViewHolder<DB> onCreateListViewHolder(@NonNull BaseAdapterRvMultipleList<BEANS> adapter, @NonNull ViewGroup parent, @LayoutRes int layoutId) {
            return new BaseViewHolder<>(parent, layoutId);
        }

        /**
         * 注释同{@link BaseAdapterRvList#onBindListViewHolder}
         */
        default void onBindListViewHolder(@NonNull BaseAdapterRvMultipleList<BEANS> adapter, @NonNull BaseViewHolder<DB> holder, int listPosition, @NonNull BEANS bean) {
        }
    }
}
