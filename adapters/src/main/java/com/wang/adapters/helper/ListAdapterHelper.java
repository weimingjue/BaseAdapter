package com.wang.adapters.helper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.adapters.BR;
import com.wang.adapters.utils.GenericUtils;
import com.wang.container.holder.BaseViewHolder;
import com.wang.container.interfaces.IListAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class ListAdapterHelper<DB extends ViewDataBinding, BEAN> {

    public static final int TYPE_BODY = 0, TYPE_HEADER = 1, TYPE_FOOTER = 2;

    @IntDef({TYPE_BODY, TYPE_HEADER, TYPE_FOOTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AdapterListType {
    }//该变量只能传入上面几种,否则会报错

    @NonNull
    public final List<BEAN> mList;
    @LayoutRes
    public int mLayoutId;

    private FrameLayout mHeaderFl, mFooterFl;

    private final IListAdapter mAdapter;

    public ListAdapterHelper(IListAdapter adapter, @LayoutRes int layoutId, @Nullable List<BEAN> list) {
        mAdapter = adapter;
        mLayoutId = layoutId;
        mList = list == null ? new ArrayList<>() : list;
    }

    public int getListPosition(int adapterPosition) {
        if (getHeaderView() != null) {
            adapterPosition--;
        }
        return adapterPosition;
    }

    public int getItemCount() {
        int count = 0;
        if (getHeaderView() != null) {
            count++;
        }
        if (getFooterView() != null) {
            count++;
        }
        count += mList.size();
        return count;
    }

    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
            case TYPE_FOOTER:
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

    @NonNull
    public final BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @AdapterListType int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new BaseViewHolder(mHeaderFl);
            case TYPE_FOOTER:
                return new BaseViewHolder(mFooterFl);
            case TYPE_BODY:
            default:
                return mAdapter.onCreateListViewHolder(parent);
        }
    }

    @AdapterListType
    public final int getItemViewType(int position) {
        if (getHeaderView() != null && position == 0) {
            return TYPE_HEADER;
        }
        if (getFooterView() != null && getItemCount() == position + 1) {
            return TYPE_FOOTER;
        }
        return TYPE_BODY;
    }

    /**
     * 初始化header、footer的基本信息
     */
    private void createHeaderFooterInfo(@Nullable View view) {
        if (view != null) {
            if (mHeaderFl == null) {
                mHeaderFl = new FrameLayout(view.getContext());
                mHeaderFl.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            if (mFooterFl == null) {
                mFooterFl = new FrameLayout(view.getContext());
                mFooterFl.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            //如果没有params，默认添加match、wrap
            if (view.getLayoutParams() == null) {
                view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    @NonNull
    public BaseViewHolder<DB> onCreateDefaultViewHolder(@NonNull ViewGroup parent, Class baseClass, Class childClass) {
        if (mLayoutId == 0) {
            mLayoutId = GenericUtils.getGenericRes(parent.getContext(), baseClass, childClass);
        }
        return new BaseViewHolder<>(parent, mLayoutId);
    }

    /**
     * header、footer不复用，点击事件自己写
     *
     * @param view null表示删除，view的parent为FrameLayout，默认match、wrap
     */
    public void setHeaderView(@Nullable View view) {
        createHeaderFooterInfo(view);
        if (view == getHeaderView()) {//相同则忽略
            return;
        }

        //就3种情况
        if (view == null && getHeaderView() != null) {
            mHeaderFl.removeAllViews();
            if (mAdapter instanceof RecyclerView.Adapter) {
                ((RecyclerView.Adapter) mAdapter).notifyItemRemoved(0);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        } else if (view != null && getHeaderView() == null) {
            mHeaderFl.addView(view);
            if (mAdapter instanceof RecyclerView.Adapter) {
                ((RecyclerView.Adapter) mAdapter).notifyItemInserted(0);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mHeaderFl.removeAllViews();
            mHeaderFl.addView(view);
        }
    }

    @Nullable
    public View getHeaderView() {
        if (mHeaderFl != null && mHeaderFl.getChildCount() > 0) {
            return mHeaderFl.getChildAt(0);
        }
        return null;
    }

    /**
     * header、footer不复用，点击事件自己写
     *
     * @param view null表示删除，view的parent为FrameLayout，默认match、wrap
     */
    public void setFooterView(@Nullable View view) {
        createHeaderFooterInfo(view);
        if (view == getFooterView()) {//相同则忽略
            return;
        }

        //就3种情况
        if (view == null && getFooterView() != null) {
            mFooterFl.removeAllViews();
            if (mAdapter instanceof RecyclerView.Adapter) {
                ((RecyclerView.Adapter) mAdapter).notifyItemRemoved(getItemCount());//count已经减一了，所以不用减了
            } else {
                mAdapter.notifyDataSetChanged();
            }
        } else if (view != null && getFooterView() == null) {
            mFooterFl.addView(view);
            if (mAdapter instanceof RecyclerView.Adapter) {
                ((RecyclerView.Adapter) mAdapter).notifyItemInserted(getItemCount() - 1);//count已经加一了，所以需要减掉
            } else {
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mFooterFl.removeAllViews();
            mFooterFl.addView(view);
        }
    }

    @Nullable
    public View getFooterView() {
        if (mFooterFl != null && mFooterFl.getChildCount() > 0) {
            return mFooterFl.getChildAt(0);
        }
        return null;
    }
}
