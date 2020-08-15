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
 * 无限循环滑动的adapter
 */
public abstract class BaseAdapterRvListCycle<DB extends ViewDataBinding, BEAN> extends BaseAdapterRv
        implements IListAdapter<BEAN, DB, OnItemClickListener> {

    private final ListAdapterHelper<DB, BEAN> mHelper;

    /**
     * 资源id已经不是必须的了
     * <p>
     * 无资源id有2种解决方式（任选其一）：
     * 1.什么都不做，根据泛型自动获取，但Proguard不能混淆{@link ViewDataBinding}的子类
     * 2.覆盖{@link #onCreateViewHolder2}，自己自定义即可
     */
    public BaseAdapterRvListCycle() {
        this(null);
    }

    public BaseAdapterRvListCycle(@Nullable List<BEAN> list) {
        this(0, list);
    }


    public BaseAdapterRvListCycle(@LayoutRes int layoutId, @Nullable List<BEAN> list) {
        mHelper = new ListAdapterHelper<>(this, layoutId, list);
    }

    @Override
    public final int getItemCount() {
        return getList().isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    @Override
    protected final void onBindViewHolder2(@NonNull BaseViewHolder holder, int position) {
        //对position进行了%处理
        position = position % getList().size();
        //noinspection unchecked
        onBindListViewHolder(holder, position, getList().get(position));
    }

    /**
     * 不支持header、footer
     */
    @Override
    public void setHeaderView(@Nullable View view) {
    }

    @Nullable
    @Override
    public View getHeaderView() {
        return null;
    }

    @Override
    public void setFooterView(@Nullable View view) {
    }

    @Nullable
    @Override
    public View getFooterView() {
        return null;
    }


    @NonNull
    @Override
    protected final BaseViewHolder<DB> onCreateViewHolder2(@NonNull ViewGroup parent, int viewType) {
        return onCreateListViewHolder(parent);
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

    /**
     * 获取指定bean
     */
    @NonNull
    public BEAN get(int position) {
        return getList().get(position % getList().size());
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
        return mHelper.onCreateDefaultViewHolder(parent, BaseAdapterLvsListCycle.class, getClass());
    }
}
