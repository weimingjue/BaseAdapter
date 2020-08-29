package com.wang.example;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.container.interfaces.IAdapter;

import java.util.List;

public interface BindingAdapters {

    /**
     * 居然需要再撸一遍，不然不会提示
     */
    @BindingAdapter({"setItemViewClick"})
    static void setItemViewClickBinding(@NonNull View view, @NonNull IAdapter adapter) {
        IAdapter.setItemViewClickBinding(view, adapter);
    }

    @BindingAdapter({"setItemRvDataAdapter", "setItemRvDataList"})
    static void setItemRvDataBinding(@NonNull RecyclerView rv, @NonNull IAdapter<?> adapter, @Nullable List<?> adapterList) {
        IAdapter.setItemRvDataBinding(rv, adapter, adapterList);
    }
}
