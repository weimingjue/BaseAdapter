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
 * RecyclerView listAdapter的基类
 * 可以加header、footer,如果是Grid需要自行处理setSpanSizeLookup头尾的跨度
 * {@link #notifyItemChanged}相关方法时注意有header时需要-1
 * bug:{@link #notifyItemChanged}方法不能刷新header、footer（header、footer不需要刷新，仅仅是先记着）
 */
public abstract class BaseAdapterRvList<DB extends ViewDataBinding, BEAN> extends BaseAdapterRv
        implements IListAdapter<BEAN, DB, OnItemClickListener> {

    private final ListAdapterHelper<DB, BEAN> mHelper;

    /**
     * 资源id已经不是必须的了
     * <p>
     * 无资源id有2种解决方式（任选其一）：
     * 1.什么都不做，根据泛型自动获取，但Proguard不能混淆{@link ViewDataBinding}的子类
     * 2.覆盖{@link #onCreateListViewHolder}，自己自定义即可
     */
    public BaseAdapterRvList() {
        this(null);
    }

    public BaseAdapterRvList(@Nullable List<BEAN> list) {
        this(0, list);
    }


    public BaseAdapterRvList(@LayoutRes int layoutId, @Nullable List<BEAN> list) {
        mHelper = new ListAdapterHelper<>(this, layoutId, list);
    }

    @Override
    public final int getItemCount() {
        return mHelper.getItemCount();
    }

    @Override
    protected final void onBindViewHolder2(@NonNull BaseViewHolder holder, int position) {
        mHelper.onBindViewHolder(holder, position);
    }

    @NonNull
    @Override
    protected final BaseViewHolder onCreateViewHolder2(@NonNull ViewGroup parent, @ListAdapterHelper.AdapterListType int viewType) {
        return mHelper.onCreateViewHolder(parent, viewType);
    }

    @ListAdapterHelper.AdapterListType
    @Override
    public final int getItemViewType(int position) {
        return mHelper.getItemViewType(position);
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
        return mHelper.onCreateDefaultViewHolder(parent, BaseAdapterRvList.class, getClass());
    }

    /**
     * @param view null表示删除
     */
    public void setHeaderView(@Nullable View view) {
        mHelper.setHeaderView(view);
    }

    @Nullable
    @Override
    public View getHeaderView() {
        return mHelper.getHeaderView();
    }

    /**
     * @param view null表示删除
     */
    @Override
    public void setFooterView(@Nullable View view) {
        mHelper.setFooterView(view);
    }

    @Nullable
    @Override
    public View getFooterView() {
        return mHelper.getFooterView();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 追加一个懒汉写法
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 懒得不能再懒了的写法
     * 就资源id，数据在xml里修改
     *
     * @param layoutId create时的资源id
     */
    public static <BEAN> BaseAdapterRvList<?, BEAN> createAdapter(@LayoutRes final int layoutId) {
        return createAdapter(null, layoutId, null);
    }

    public static <DB extends ViewDataBinding, BEAN> BaseAdapterRvList<DB, BEAN> createAdapter
            (@Nullable List<BEAN> list, @LayoutRes final int layoutId, @Nullable final OnAdapterBindListener<DB, BEAN> listener) {
        return new BaseAdapterRvList<DB, BEAN>(layoutId, list) {

            @NonNull
            @Override
            public BaseViewHolder<DB> onCreateListViewHolder(@NonNull ViewGroup parent) {
                BaseViewHolder<DB> holder = super.onCreateListViewHolder(parent);
                if (listener != null) {
                    listener.onViewHolderCreated(holder);
                }
                return holder;
            }

            @Override
            public void onBindListViewHolder(@NonNull BaseViewHolder<DB> holder, int listPosition, BEAN bean) {
                if (listener != null) {
                    listener.onBindViewHolder(holder, listPosition, bean);
                }
            }
        };
    }

    public interface OnAdapterBindListener<DB extends ViewDataBinding, BEAN> {

        /**
         * 当viewHolder创建完成后
         */
        default void onViewHolderCreated(BaseViewHolder<DB> holder) {
        }

        void onBindViewHolder(BaseViewHolder<DB> holder, int listPosition, BEAN bean);
    }
}
