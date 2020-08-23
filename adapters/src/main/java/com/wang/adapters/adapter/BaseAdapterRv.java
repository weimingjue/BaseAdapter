package com.wang.adapters.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.adapters.R;
import com.wang.adapters.interfaces.OnItemClickListener;
import com.wang.adapters.interfaces.OnItemItemClickListener;
import com.wang.container.holder.BaseViewHolder;
import com.wang.container.interfaces.IAdapter;
import com.wang.container.interfaces.IListAdapter;

import java.util.List;

/**
 * 适用于rv、我自定义的{@link BaseSuperAdapter}
 * 增加点击事件
 */
public abstract class BaseAdapterRv extends RecyclerView.Adapter<BaseViewHolder>
        implements BaseSuperAdapter.ISuperAdapter, IAdapter<OnItemClickListener> {

    public final String TAG = getClass().getSimpleName();
    protected OnItemClickListener mListener;

    @Override
    public final BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder holder = onCreateViewHolder2(parent, viewType);
        holder.itemView.setTag(R.id.tag_view_holder, holder);
        holder.itemView.setTag(R.id.tag_view_adapter, this);
        return holder;
    }

    @Override
    public final void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        //设置点击事件
        holder.itemView.setOnClickListener(mListener);
        holder.itemView.setOnLongClickListener(mListener);
        //防止null时抢占事件
        holder.itemView.setClickable(mListener != null);
        holder.itemView.setLongClickable(mListener != null);
        onBindViewHolder2(holder, position);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是可能用到的父类方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 自定义的{@link BaseSuperAdapter}的多条目用到,获取当前item所占条目数
     */
    @Override
    public int getSpanSize(int position) {
        return 1;
    }

//    @Override
//    public int getItemViewType(int position) {
//        return 0;
//    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是增加的方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 给view设置点击事件到{@link #mListener}中
     * <p>
     * 点击回调见{@link #setOnItemClickListener}{@link OnItemClickListener}
     */
    protected final void setItemViewClick(View view, BaseViewHolder holder) {
        view.setTag(R.id.tag_view_holder, holder);
        view.setTag(R.id.tag_view_adapter, this);
        if (!(view instanceof RecyclerView)) view.setOnClickListener(mListener);
    }

    /**
     * 给rv设置点击事件和数据
     * 点击回调必须使用{@link OnItemItemClickListener}，否则回调将会错乱
     */
    protected final void setItemRvData(RecyclerView rv, BaseViewHolder holder, List<?> adapterList) {
        rv.setTag(R.id.tag_view_holder, holder);
        rv.setTag(R.id.tag_view_adapter, this);
        IListAdapter adapter = (IListAdapter) rv.getAdapter();
        //noinspection ConstantConditions,unchecked
        adapter.setOnItemClickListener(mListener);
        //noinspection unchecked 忽略未检查错误,如果出异常说明你传的list和你的adapter对不上
        adapter.setListAndNotifyDataSetChanged(adapterList);
    }

    protected abstract void onBindViewHolder2(@NonNull BaseViewHolder holder, int position);

    @NonNull
    protected abstract BaseViewHolder onCreateViewHolder2(@NonNull ViewGroup parent, int viewType);

    /**
     * 里面回调里也有{@link OnItemClickListener#onItemLongClick}、header、footer点击长按
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mListener = listener;
        notifyDataSetChanged();
    }
}