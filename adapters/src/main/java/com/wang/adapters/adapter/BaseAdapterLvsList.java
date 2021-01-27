package com.wang.adapters.adapter;

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
 * 和{@link BaseAdapterRvList}基本一致，适用于listView、gridView、viewPager
 */
public abstract class BaseAdapterLvsList<DB extends ViewDataBinding, BEAN> extends BaseAdapterLvs
        implements IListAdapter<BEAN, DB, OnItemClickListener> {

    private final ListAdapterHelper<DB, BEAN> mHelper;

    /**
     * 资源id已经不是必须的了
     * <p>
     * 无资源id有2种解决方式（任选其一）：
     * 1.什么都不做，根据泛型自动获取，但Proguard不能混淆{@link ViewDataBinding}的子类
     * 2.覆盖{@link #onCreateListViewHolder}，自己自定义即可
     */
    public BaseAdapterLvsList() {
        this(null);
    }

    public BaseAdapterLvsList(@Nullable List<BEAN> list) {
        this(0, list);
    }


    public BaseAdapterLvsList(@LayoutRes int layoutId, @Nullable List<BEAN> list) {
        mHelper = new ListAdapterHelper<>(this, layoutId, list);
    }

    @Override
    public final int getItemCount() {
        return mHelper.getItemCount();
    }

    @NonNull
    @Override
    protected final BaseViewHolder onCreateViewHolder2(@NonNull ViewGroup parent, @ListAdapterHelper.AdapterListType int viewType) {
        return mHelper.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected final void onBindViewHolder2(@NonNull BaseViewHolder holder, int position) {
        mHelper.onBindViewHolder(holder, position);
    }

    @ListAdapterHelper.AdapterListType
    @Override
    public final int getItemViewType(int position) {
        return mHelper.getItemViewType(position);
    }

    /**
     * 几百年没用，居然忘了lv还必须重写这个方法了
     */
    @Override
    public int getViewTypeCount() {
        return 3;
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

    /**
     * 最终你的list的create
     * <p>
     * 默认用DataBinding create
     * 完全不需要的话覆盖整个方法就行了，不会出问题
     * 你也可以重写来添加自己的默认逻辑，如：全局隐藏显示、嵌套rv的默认属性设置等
     */
    @NonNull
    @Override
    public BaseViewHolder<DB> onCreateListViewHolder(@NonNull ViewGroup parent) {
        return mHelper.onCreateDefaultViewHolder(parent, BaseAdapterLvsList.class, getClass());
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
}
