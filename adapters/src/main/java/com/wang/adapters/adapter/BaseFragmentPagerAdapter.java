package com.wang.adapters.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * vp的adapter的fragment的终极封装
 * FragmentPagerAdapter>当destroyItem时并不会销毁frag,只是解绑视图了,所以刷新frag集合的方式不能删除frag
 * 如果想删除{@link BaseFragmentStatePagerAdapter}
 */
public final class BaseFragmentPagerAdapter extends FragmentPagerAdapter {
    public final String TAG = getClass().getSimpleName();

    private final ArrayList<Fragment> mFragments = new ArrayList<>();//添加的Fragment的集合

    private List<? extends CharSequence> mTitles;//tabLayout总是取title

    public BaseFragmentPagerAdapter(FragmentManager fm, Fragment... frags) {
        super(fm);
        addFragment(frags);
    }

    public BaseFragmentPagerAdapter(FragmentManager fm, List<? extends Fragment> frags) {
        super(fm);
        addFragment(frags);
    }

    public BaseFragmentPagerAdapter addFragment(Fragment... frags) {
        Collections.addAll(mFragments, frags);
        notifyDataSetChanged();
        return this;
    }

    public BaseFragmentPagerAdapter addFragment(List<? extends Fragment> frags) {
        mFragments.addAll(frags);
        notifyDataSetChanged();
        return this;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        //得到对应position的Fragment
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        //返回Fragment的数量
        return mFragments.size();
    }

    public BaseFragmentPagerAdapter setTitles(CharSequence... titles) {
        return setTitles(Arrays.asList(titles));
    }

    /**
     * 添加frag的title，类似TabLayout可能会用到
     */
    public BaseFragmentPagerAdapter setTitles(List<? extends CharSequence> titles) {
        mTitles = titles;
        notifyDataSetChanged();
        return this;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null && position >= 0 && position < mTitles.size())
            return mTitles.get(position);

        return null;
    }
}
