package com.wang.example;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.wang.adapters.adapter.BaseFragmentNotifyAdapter;

import java.util.ArrayList;

public class ViewPagerFragActivity extends AppCompatActivity {

    private ViewPager mVp;
    private BaseFragmentNotifyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager_frag);
        findViewById(R.id.tv_viewpagerfrag_ShuaXin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//当点击时随机修改数据
                MyFrag baoLiu = (MyFrag) mAdapter.getFragments().get(2);//要保留的frag
                mAdapter.getFragments().clear();//重新加载数据
                for (int i = 0; i < 2; i++) {
                    mAdapter.getFragments().add(MyFrag.newInstance("新的" + System.currentTimeMillis()));//换成新的
                }
                mAdapter.getFragments().add(baoLiu);
                baoLiu.changeTv("保留的tv修改数据" + System.currentTimeMillis());
                for (int i = mAdapter.getFragments().size(); i < 5; i++) {
                    mAdapter.getFragments().add(MyFrag.newInstance("新的" + System.currentTimeMillis()));//换成新的
                }
                mAdapter.notifyAllItem(2);//保留当前frag，刷新数据
            }
        });
        mVp = findViewById(R.id.vp_viewpagerfrag);
//        普通的使用
//        mVp.setAdapter(new BaseAdapterVpFrag(getSupportFragmentManager(),MyFrag.newInstance("1"),MyFrag.newInstance("2")));

//        frag动态变化的简单使用
//        ArrayList<MyFrag> list = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            list.add(MyFrag.newInstance(i * 999999999L + ""));
//        }
//        BaseAdapterVpStateFrag adapter = new BaseAdapterVpStateFrag(getSupportFragmentManager(), list);
//        mVp.setAdapter(adapter);
//        ...
//        list.remove(0);
//        list.add(MyFrag.newInstance("这是新的frag"));
//        adapter.notifyDataSetChanged();

//        主要介绍解决网络加载动态刷新白屏的问题
        //添加初始值
        ArrayList<MyFrag> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(MyFrag.newInstance("默认值" + i * 999999999L));
        }
        mAdapter = new BaseFragmentNotifyAdapter(getSupportFragmentManager(), list);
        mVp.setAdapter(mAdapter);
    }

    public static class MyFrag extends Fragment {

        private TextView mTv;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView tv = new TextView(container.getContext());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return tv;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mTv = (TextView) view;
            mTv.setBackgroundColor(0xffffffff);//恢复到初始值
            mTv.setText("初始值");//恢复到初始值
            mTv.setTextSize(10);
            new Handler().postDelayed(new Runnable() {//只是为了模拟网络加载时的白屏，切勿模仿
                @Override
                public void run() {
                    mTv.setText(getArguments().getString("B_A"));
                    mTv.setTextSize(40);
                    mTv.setBackgroundColor(0xffff00ff);
                }
            }, 100);
        }

        public void changeTv(String text) {
            if (mTv != null) {
                mTv.setText(text);
            }
            if (getArguments() != null) {
                getArguments().putString("B_A", text);
            }
        }

        public static MyFrag newInstance(String text) {
            Bundle args = new Bundle();
            args.putString("B_A", text);
            MyFrag fragment = new MyFrag();
            fragment.setArguments(args);
            return fragment;
        }
    }
}
