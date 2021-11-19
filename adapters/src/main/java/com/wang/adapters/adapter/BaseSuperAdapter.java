package com.wang.adapters.adapter;

import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.adapters.interfaces.OnItemClickListener;
import com.wang.container.holder.BaseViewHolder;
import com.wang.container.interfaces.IAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个超级adapter可以添加其他adapter
 * 适用于类似天猫淘宝首页上下布局功能区分明显的（分类>推荐>轮播图>抢购>商品...），明显可以拆分成几个小adapter的
 * <p>
 * 限制条件：
 * 1.所有的adapter的type必须在{@link #mTypeMax}{@link #mTypeMin}之间
 * 2.目前只支持{@link #notifyDataSetChanged}，动画等效果暂不支持
 *
 * @deprecated rv已经出了新的{@link ConcatAdapter}，用法基本完全一致并且支持更多
 */
@Deprecated
public final class BaseSuperAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    public final String TAG = getClass().getSimpleName();
    protected ArrayList<ISuperAdapter> mItemAdapters = new ArrayList<>();

    public static final int mTypeMax = 100000, mTypeMin = -100000, mTypeMinus = mTypeMax - mTypeMin;
    protected RecyclerView mRecyclerView;
    protected GridLayoutManager mLayoutManager;

    protected RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            notifyDataSetChanged();
        }
    };

    /**
     * 子adapter的信息,具体见{@link MyItemInfo#refreshItemInfo}
     */
    protected MyItemInfo mItemInfo = new MyItemInfo();

    /**
     * GridLayoutManager自动识别,但是setLayoutManager(glm)这个方法必须在addAdapter之前或在RecyclerView.setAdapter()之前.
     * 不是GridLayoutManager类型的可以忽略.
     * 例:错误的写法mRv.setAdapter(new BaseSuperAdapter(mActivity).addAdapter(new XxxAdapter()));
     * mRv.setLayoutManager(new GridLayoutManager(mActivity, 2));
     * 需要先setLayoutManager然后再做其他操作,原因见{@link #checkLayoutManager}.
     */
    public BaseSuperAdapter() {
        this(null);
    }

    /**
     * @param manager 如果是GridLayoutManager需要用到setSpanSizeLookup这个方法
     */
    public BaseSuperAdapter(GridLayoutManager manager) {
        changedLayoutManager(manager);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return (BaseViewHolder) mItemAdapters.get(viewType / mTypeMinus).createViewHolder(parent, viewType % mTypeMinus);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        mItemInfo.refreshItemInfo(position);
        //noinspection unchecked 未检查警告,此处忽略
        mItemAdapters.get(mItemInfo.mAdapterPosition).bindViewHolder(holder, mItemInfo.mItemPosition);
    }

    @Override
    public int getItemViewType(int position) {
        mItemInfo.refreshItemInfo(position);
        int itemType = mItemAdapters.get(mItemInfo.mAdapterPosition).getItemViewType(mItemInfo.mItemPosition);
        if (itemType < mTypeMin || itemType >= mTypeMax)
            throw new RuntimeException("你的type必须在" + mTypeMin + "~" + mTypeMax + "之间");
        return mItemInfo.mAdapterPosition * mTypeMinus + itemType;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (ISuperAdapter adapter : mItemAdapters) {
            count += adapter.getItemCount();
        }
        return count;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        checkLayoutManager();
    }

    protected void checkLayoutManager() {
        if (mRecyclerView == null) return;
        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        if ((mLayoutManager == null || mLayoutManager != manager) && manager instanceof GridLayoutManager) {
            changedLayoutManager((GridLayoutManager) manager);
        }
    }

    protected class MyItemInfo {
        /**
         * 使用之前请调用{@link #refreshItemInfo}
         * {@link #mItemAdapters}list的position,子adapter的所需要的相对position
         */
        private int mAdapterPosition, mItemPosition;

        /**
         * 根据超级adapter的position返回子adapter的信息
         */
        private MyItemInfo refreshItemInfo(int position) {
            //itemAdapter的position=0时的真实位置
            int itemStartPosition = 0;
            for (int i = 0; i < mItemAdapters.size(); i++) {
                int itemCount = mItemAdapters.get(i).getItemCount();
                int nextStartPosition = itemStartPosition + itemCount;
                //下一个adapter的位置比position大说明当前type就在这个adapter中
                if (nextStartPosition > position) {
                    mItemInfo.mAdapterPosition = i;
                    mItemInfo.mItemPosition = position - itemStartPosition;
                    return mItemInfo;
                } else {
                    //循环相加
                    itemStartPosition = nextStartPosition;
                }
            }
            throw new RuntimeException("没有取到对应的type,可能你没有(及时)刷新adapter");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是增加的方法
    ///////////////////////////////////////////////////////////////////////////

    public BaseSuperAdapter addAdapter(@NonNull ISuperAdapter... adapters) {
        for (ISuperAdapter adapter : adapters) {
            adapter.registerAdapterDataObserver(mObserver);
            mItemAdapters.add(adapter);
        }
        checkLayoutManager();
        notifyDataSetChanged();
        return this;
    }

    public BaseSuperAdapter addAdapter(@NonNull List<? extends ISuperAdapter> adapters) {
        for (ISuperAdapter adapter : adapters) {
            adapter.registerAdapterDataObserver(mObserver);
            mItemAdapters.add(adapter);
        }
        checkLayoutManager();
        notifyDataSetChanged();
        return this;
    }

    /**
     * 删除指定adapter
     */
    public BaseSuperAdapter removeAdapter(ISuperAdapter adapter) {
        return removeAdapter(mItemAdapters.indexOf(adapter));
    }

    /**
     * 删除指定adapter
     */
    public BaseSuperAdapter removeAdapter(int position) {
        if (position > -1 && position < mItemAdapters.size()) {
            mItemAdapters.remove(position).unregisterAdapterDataObserver(mObserver);
            notifyDataSetChanged();
        }
        return this;
    }

    /**
     * 把rv的LayoutManager改成其他的GridLayoutManager时.此方法理论上没啥用
     */
    public void changedLayoutManager(GridLayoutManager manager) {
        if (manager == null) return;
        mLayoutManager = manager;
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                mItemInfo.refreshItemInfo(position);
                return mItemAdapters.get(mItemInfo.mAdapterPosition).getSpanSize(mItemInfo.mItemPosition);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 供外部使用的接口和实现类
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * adapter的接口，见实现类{@link BaseAdapterRv}{@link BaseAdapterRvList}
     */
    public interface ISuperAdapter extends IAdapter<OnItemClickListener> {
        /**
         * observe主要用于notify
         * 由于recyclerview的notify方法很多,此处只使用lv的observe
         */
        void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer);

        void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer);

        int getSpanSize(int position);

        /**
         * @return 必须在注解范围之内
         */
        @IntRange(from = BaseSuperAdapter.mTypeMin, to = BaseSuperAdapter.mTypeMax)
        int getItemViewType(int position);
    }
}