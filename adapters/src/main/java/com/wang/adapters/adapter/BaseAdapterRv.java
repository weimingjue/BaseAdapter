package com.wang.adapters.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.adapters.R;
import com.wang.adapters.interfaces.OnItemClickListener;
import com.wang.container.holder.BaseViewHolder;
import com.wang.container.interfaces.IAdapter;

/**
 * 适用于rv、我自定义的{@link BaseSuperAdapter}
 * 增加点击事件
 */
public abstract class BaseAdapterRv extends RecyclerView.Adapter<BaseViewHolder>
        implements BaseSuperAdapter.ISuperAdapter, IAdapter<OnItemClickListener> {

    public final String TAG = getClass().getSimpleName();
    protected OnItemClickListener mListener;
    protected BaseViewHolder mBindTempViewHolder;

    @Override
    public final BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder holder = onCreateViewHolder2(parent, viewType);
        holder.itemView.setTag(R.id.tag_view_holder, holder);
        holder.itemView.setTag(R.id.tag_view_adapter, this);
        return holder;
    }

    @Override
    public final void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        mBindTempViewHolder = holder;
        //设置点击事件
        holder.itemView.setOnClickListener(mListener);
        holder.itemView.setOnLongClickListener(mListener);
        //防止null时抢占事件
        holder.itemView.setClickable(mListener != null);
        holder.itemView.setLongClickable(mListener != null);
        onBindViewHolder2(holder, position);
    }

    /**
     * 正在bind时的ViewHolder，方便xml中使用dataBinding设置点击事件
     */
    public BaseViewHolder getBindTempViewHolder() {
        return mBindTempViewHolder;
    }

    @Nullable
    @Override
    public OnItemClickListener getOnItemClickListener() {
        return mListener;
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