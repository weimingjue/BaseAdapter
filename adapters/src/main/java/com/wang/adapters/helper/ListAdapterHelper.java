package com.wang.adapters.helper;

import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import com.wang.adapters.BR;
import com.wang.adapters.utils.GenericUtils;
import com.wang.container.helper.BaseListAdapterHelper;
import com.wang.container.holder.BaseViewHolder;
import com.wang.container.interfaces.IListAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * 适用于简单list样式的公共代码
 */
public class ListAdapterHelper<DB extends ViewDataBinding, BEAN> extends BaseListAdapterHelper<BEAN> {

    public static final int TYPE_BODY = 0, TYPE_HEADER = 1, TYPE_FOOTER = 2;

    @IntDef({TYPE_BODY, TYPE_HEADER, TYPE_FOOTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AdapterListType {
    }//该变量只能传入上面几种,否则会报错

    @LayoutRes
    public int mLayoutId;

    public ListAdapterHelper(IListAdapter adapter, @LayoutRes int layoutId, @Nullable List<BEAN> list) {
        super(adapter, list);
        mLayoutId = layoutId;
    }

    public int getListPosition(int adapterPosition) {
        if (mHeaderView != null) {
            adapterPosition--;
        }
        return adapterPosition;
    }

    public int getItemCount() {
        int count = 0;
        if (mHeaderView != null) {
            count++;
        }
        if (mFooterView != null) {
            count++;
        }
        count += mList.size();
        return count;
    }

    @NonNull
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @AdapterListType int viewType) {
        switch (viewType) {
            case TYPE_HEADER://每次create都会new一个布局，防止多次create导致崩溃
            case TYPE_FOOTER:
                return onCreateHeaderFooterViewHolder(parent);
            case TYPE_BODY:
            default:
                return mAdapter.onCreateListViewHolder(parent);
        }
    }

    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                onBindHeaderFooterViewHolder(holder, mHeaderView);
                return;
            case TYPE_FOOTER:
                onBindHeaderFooterViewHolder(holder, mFooterView);
                return;
            case TYPE_BODY:
                int listPosition = getListPosition(position);
                if (holder.getBinding() != null) {
                    holder.getBinding().setVariable(BR.bean, mList.get(listPosition));
                }
                //noinspection unchecked
                mAdapter.onBindListViewHolder((BaseViewHolder<DB>) holder, listPosition, mList.get(listPosition));
                if (holder.getBinding() != null) {
                    holder.getBinding().executePendingBindings();
                }
        }
    }

    @AdapterListType
    public int getItemViewType(int position) {
        if (mHeaderView != null && position == 0) {
            return TYPE_HEADER;
        }
        if (mFooterView != null && getItemCount() == position + 1) {
            return TYPE_FOOTER;
        }
        return TYPE_BODY;
    }

    @NonNull
    public BaseViewHolder<DB> onCreateDefaultViewHolder(@NonNull ViewGroup parent, Class baseClass, Class childClass) {
        if (mLayoutId == 0) {
            mLayoutId = GenericUtils.getGenericRes(parent.getContext(), baseClass, childClass);
        }
        return new BaseViewHolder<>(parent, mLayoutId);
    }
}
