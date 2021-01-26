package com.wang.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wang.adapters.adapter.BaseAdapterRvList;
import com.wang.adapters.interfaces.OnItemItemClickListener;
import com.wang.container.holder.BaseViewHolder;
import com.wang.example.databinding.AdapterMainListBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_main_vp).setOnClickListener(v -> startActivity(ViewPagerFragActivity.class));
        mRv = findViewById(R.id.rv_main);
        listTest();
    }

    /**
     * 简单的列表测试
     */
    private void listTest() {
        mRv.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<TestBean> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TestBean tb = new TestBean();
            tb.text = "第" + i;
            if (i % 5 == 0) {
                tb.itemTextList = new ArrayList<>();
                for (int j = 0; j < 10; j++) {
                    tb.itemTextList.add("第" + i + "，子" + j);
                }
            }
            list.add(tb);
        }
        //直接create，逻辑较少推荐使用
//        BaseAdapterRvList<?, String> adapter = BaseAdapterRvList.createAdapter(R.layout.adapter_main_list);
//        BaseAdapterRvList<AdapterMainListBinding, String> adapter = BaseAdapterRvList.createAdapter(null, R.layout.adapter_main_list,
//                (holder, listPosition, s) -> {
//                    if (s.contains("10")) {
//                        holder.itemView.setBackgroundColor(0xff999999);
//                    }
//                });
        //自定义方案
        ListAdapter adapter = new ListAdapter();
        //多条目
//        BaseAdapterRvMultipleList<TestBean> adapter = new BaseAdapterRvMultipleList<>();
//        adapter.addMultipleItem(R.layout.adapter_main_multiple_0, new BaseAdapterRvMultipleList.OnMultipleListListener<AdapterMainMultiple0Binding, TestBean>() {
//            @Override
//            public boolean isThisType(@NonNull BaseAdapterRvMultipleList<TestBean> adapter, int listPosition, @NonNull TestBean bean) {
//                return listPosition % 2 == 0;
//            }
//
//            @Override
//            public void onBindListViewHolder(@NonNull BaseAdapterRvMultipleList<TestBean> adapter, @NonNull BaseViewHolder<AdapterMainMultiple0Binding> holder, int listPosition, @NonNull TestBean bean) {
//                holder.getBinding().tvHhh.setTextColor(0xff00ff00);
//            }
//        }).addMultipleItem(R.layout.adapter_main_multiple_1, (adapter1, listPosition, bean) -> listPosition % 2 != 0);

        adapter.setListAndNotifyDataSetChanged(list);
        mRv.setAdapter(adapter);
        //设置点击事件
        adapter.setOnItemClickListener(new OnItemItemClickListener() {
            @Override
            public void onParentItemClick(@NonNull View view, int parentPosition) {
                switch (view.getId()) {
                    case R.id.bt_button:
                        toast("没想到吧，还能这样玩：" + parentPosition);
                        break;
                    default:
                        toast("你点击了外层：" + parentPosition);
                        break;
                }
            }

            @Override
            public void onChildItemClick(@NonNull View view, int parentPosition, int childPosition) {
                toast("你点击了外层：" + parentPosition + "，内层：" + childPosition);
            }

            @Override
            public boolean onParentItemLongClick(@NonNull View view, int parentPosition) {
                switch (view.getId()) {
                    case R.id.bt_button:
                        toast("长按没想到吧，还能这样玩：" + parentPosition);
                        break;
                    default:
                        toast("你长按了外层：" + parentPosition);
                        break;
                }
                return true;
            }

            @Override
            public boolean onChildItemLongClick(@NonNull View view, int parentPosition, int childPosition) {
                toast("你长按了外层：" + parentPosition + "，内层：" + childPosition);
                return true;
            }

            @Override
            public void onParentHeaderClick(@NonNull View view) {
                toast("你点击了外层：header");
            }

            @Override
            public void onChildHeaderClick(@NonNull View view, int parentPosition) {
                toast("你点击了外层：" + parentPosition + "，内层：header");
            }

            @Override
            public boolean onParentHeaderLongClick(@NonNull View view) {
                toast("你长按了外层：header");
                return true;
            }

            @Override
            public boolean onChildHeaderLongClick(@NonNull View view, int parentPosition) {
                toast("你长按了外层：" + parentPosition + "，内层：header");
                return true;
            }

            @Override
            public void onParentFooterClick(@NonNull View view) {
                toast("你点击了外层：footer");
            }

            @Override
            public void onChildFooterClick(@NonNull View view, int parentPosition) {
                toast("你点击了外层：" + parentPosition + "，内层：footer");
            }

            @Override
            public boolean onParentFooterLongClick(@NonNull View view) {
                toast("你长按了外层：footer");
                return true;
            }

            @Override
            public boolean onChildFooterLongClick(@NonNull View view, int parentPosition) {
                toast("你长按了外层：" + parentPosition + "，内层：footer");
                return true;
            }
        });
        //添加头
        adapter.setHeaderView(this, R.layout.adapter_main_header);
        //添加尾
        AppCompatImageView iv = new AppCompatImageView(this);
        iv.setImageResource(R.mipmap.ic_launcher);
        iv.setAdjustViewBounds(true);
        adapter.setFooterView(iv);
    }

    public static class ListAdapter extends BaseAdapterRvList<AdapterMainListBinding, TestBean> {

        @Override
        public void onBindListViewHolder(@NonNull BaseViewHolder<AdapterMainListBinding> holder, int listPosition, TestBean bean) {
            setItemRvData(holder.getBinding().rvItemList, holder, bean.itemTextList);
            MyAdapter adapter = (MyAdapter) holder.getBinding().rvItemList.getAdapter();
            if (bean.itemTextList != null && bean.itemTextList.size() > 0) {
                TextView footerTv = new TextView(holder.getContext());
                footerTv.setText("内层的footer，外层position" + listPosition);
                footerTv.setTextColor(0xff999999);
                footerTv.setTextSize(13);
                adapter.setFooterView(footerTv);
            } else {
                adapter.setFooterView(null);
            }
        }

        @NonNull
        @Override
        public BaseViewHolder<AdapterMainListBinding> onCreateListViewHolder(@NonNull ViewGroup parent) {
            BaseViewHolder<AdapterMainListBinding> holder = super.onCreateListViewHolder(parent);
            holder.getBinding().rvItemList.setLayoutManager(new GridLayoutManager(parent.getContext(), 2));
            MyAdapter adapter = new MyAdapter();
            TextView headerTv = new TextView(parent.getContext());
            headerTv.setText("内层的header一直有");
            headerTv.setTextColor(0xff999999);
            headerTv.setTextSize(13);
            adapter.setHeaderView(headerTv);
            holder.getBinding().rvItemList.setAdapter(adapter);
            holder.getBinding().rvItemList.setNestedScrollingEnabled(false);
            return holder;
        }

        private static class MyAdapter extends BaseAdapterRvList<ViewDataBinding, String> {

            @Override
            public void onBindListViewHolder(@NonNull BaseViewHolder<ViewDataBinding> holder, int listPosition, String bean) {
                TextView tv = (TextView) holder.itemView;
                tv.setText(bean);
            }

            @NonNull
            @Override
            public BaseViewHolder<ViewDataBinding> onCreateListViewHolder(@NonNull ViewGroup parent) {
                TextView tv = new AppCompatTextView(parent.getContext());
                tv.setTextColor(0xffff00ff);
                tv.setTextSize(15);
                tv.setPadding(50, 10, 50, 10);
                return new BaseViewHolder<>(tv);
            }
        }
    }

    public static class TestBean {
        public String text;
        public List<String> itemTextList;
    }

    public void startActivity(Class c) {
        startActivity(new Intent(this, c));
    }

    public void toast(String st) {
        Toast.makeText(this, st, Toast.LENGTH_SHORT).show();
    }
}
