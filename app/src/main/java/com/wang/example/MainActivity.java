package com.wang.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.adapters.adapter.BaseAdapterRvList;
import com.wang.adapters.interfaces.OnItemClickListener;
import com.wang.container.holder.BaseViewHolder;
import com.wang.example.databinding.AdapterMainListBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_main_vp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ViewPagerFragActivity.class);
            }
        });
        mRv = findViewById(R.id.rv_main);
        listTest();
    }

    /**
     * 简单的列表测试
     */
    private void listTest() {
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        mRv.setLayoutManager(manager);
        final ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add("第" + i);
        }
//        BaseAdapterRvList<?, String> adapter = BaseAdapterRvList.createAdapter(R.layout.adapter_main_list);
        BaseAdapterRvList<?, String> adapter = new ListAdapter();
//        BaseAdapterRvList<AdapterMainListBinding, String> adapter = BaseAdapterRvList.createAdapter(null, R.layout.adapter_main_list,
//                (holder, listPosition, s) -> {
//                    if (s.contains("10")) {
//                        holder.itemView.setBackgroundColor(0xff999999);
//                    }
//                });
        adapter.setListAndNotifyDataSetChanged(list);
        mRv.setAdapter(adapter);
        //设置点击事件
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull View view, int listPosition) {
                Toast.makeText(MainActivity.this, "点击第" + list.get(listPosition), Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onItemLongClick(@NonNull View view, int listPosition) {
                Toast.makeText(MainActivity.this, "长按第" + list.get(listPosition), Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public void onFooterClick(@NonNull View view) {
                Toast.makeText(MainActivity.this, "footer被点击", Toast.LENGTH_SHORT).show();
            }
        });
        //添加尾
        AppCompatImageView iv = new AppCompatImageView(this);
        iv.setImageResource(R.mipmap.ic_launcher);
        iv.setAdjustViewBounds(true);
        adapter.setFooterView(iv);
        //GridLayoutManager需要将头或尾占多行
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == list.size() ? 2 : 1;
            }
        });
    }

    public static class ListAdapter extends BaseAdapterRvList<AdapterMainListBinding, String> {

        @Override
        public void onBindListViewHolder(@NonNull BaseViewHolder<AdapterMainListBinding> holder, int listPosition, String s) {
            if (s.contains("100")) {
                getList().set(listPosition, "改掉了100");//后面会调用刷新dataBinding
                holder.getBinding().viewBackground.setBackgroundColor(0xff00ff00);
            } else if (s.contains("10")) {
                holder.getBinding().viewBackground.setBackgroundColor(0xff999999);
            } else {
                holder.getBinding().viewBackground.setBackgroundColor(0xffffffff);
            }
        }

        @NonNull
        @Override
        public BaseViewHolder<AdapterMainListBinding> onCreateListViewHolder(@NonNull ViewGroup parent) {
            BaseViewHolder<AdapterMainListBinding> holder = super.onCreateListViewHolder(parent);
            holder.itemView.setBackgroundColor(0xffeeeeee);
            return holder;
        }
    }

    public void startActivity(Class c) {
        startActivity(new Intent(this, c));
    }
}
