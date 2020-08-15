package com.wang.adapters.adapter;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wang.adapters.utils.ArrayUtils;

import java.util.ArrayList;

/**
 * 新增:不仅仅可以回收view,也可以回收其他任何对象
 * <p>
 * 涉及多级嵌套(比如:购物车),不能很好复用,可以简单的使用ViewRecycler来管理
 * (recyclerview高写成wrap也可以嵌套，也可以共用缓存池，自己搜索)
 * <p>
 * 示例见{@link BaseAdapterLvs#mRecycler}
 */
public final class ViewRecycler<T> {

    /**
     * 最大缓存数量
     */
    @IntRange(from = 1, to = 1000)
    public int mMaxCacheSize;

    private final SparseArray<ArrayList<T>> mCaches = new SparseArray<>();

    public ViewRecycler() {
        this(10);
    }

    public ViewRecycler(@IntRange(from = 1, to = 1000) int maxCacheSize) {
        mMaxCacheSize = maxCacheSize;
    }

    /**
     * 检查是否超出缓存数量
     */
    private void checkCache(int viewType) {
        int size = 0, typePosition = 0;
        ArrayList<T> views = mCaches.get(viewType);
        for (int i = 0; i < mCaches.size(); i++) {
            ArrayList<T> vs = mCaches.valueAt(i);
            size += vs.size();
            if (views == vs) typePosition = i;
        }
        if (size > mMaxCacheSize) {
            //太多的话,删除下一个type的view
            ArrayList<T> vs = null;
            while (ArrayUtils.isEmpty(vs)) {
                vs = mCaches.valueAt((++typePosition) % mCaches.size());
            }
            vs.remove(0);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是公共方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 回收view,会remove掉回收的child
     *
     * @param vg        父
     * @param childView 要回收的view
     * @param viewType  相当于itemType,就一种传0即可
     */
    public void recycleItem(@Nullable ViewGroup vg, @NonNull T childView, int viewType) {
        recycleItem(childView, viewType);
        if (vg != null) vg.removeView((View) childView);
    }

    public void recycleItem(@NonNull ViewGroup vg, int childIndex, int viewType) {
        //noinspection unchecked
        recycleItem((T) vg.getChildAt(childIndex), viewType);
        vg.removeViewAt(childIndex);
    }

    public void recycleItem(T item, int viewType) {
        ArrayList<T> views = mCaches.get(viewType);
        if (views == null) {
            views = new ArrayList<>();
            mCaches.append(viewType, views);
        }
        views.add(item);
        checkCache(viewType);
    }

    public void recycleAllItem(ViewGroup vg, int viewType) {
        for (int i = vg.getChildCount() - 1; i >= 0; i--) {
            recycleItem(vg, i, viewType);
        }
    }

    public T get(int viewType) {
        ArrayList<T> views = mCaches.get(viewType);
        return ArrayUtils.isEmpty(views) ? null : views.remove(0);
    }

    /**
     * 获取当前type所有的view
     */
    public ArrayList<T> getTypeViews(int viewType) {
        return mCaches.get(viewType);
    }

    public void clearCache(int viewType) {
        ArrayList<T> views = mCaches.get(viewType);
        if (!ArrayUtils.isEmpty(views)) views.clear();
    }

    public void clearAllCache() {
        for (int i = 0; i < mCaches.size(); i++) {
            mCaches.valueAt(i).clear();
        }
    }
}
