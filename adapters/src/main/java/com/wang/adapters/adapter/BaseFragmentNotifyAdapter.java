package com.wang.adapters.adapter;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 解决FragmentPagerAdapter界面没有变化和FragmentStatePagerAdapter刷新短暂白屏的问题
 * 刷新请使用{@link #notifyAllItem}
 */
public final class BaseFragmentNotifyAdapter extends PagerAdapter {
    public final String TAG = getClass().getSimpleName();

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;//当前正在执行的事务
    private Fragment mCurrentPrimaryItem = null;//当前的frag

    private final ArrayList<Fragment> mFragments = new ArrayList<>();//添加的Fragment的集合

    private List<? extends CharSequence> mTitles;//tabLayout总是取title
    private int mNoDestroyPosition = POSITION_UNCHANGED;

    public BaseFragmentNotifyAdapter(FragmentManager fm, Fragment... frags) {
        mFragmentManager = fm;
        addFragment(frags);
    }

    public BaseFragmentNotifyAdapter(FragmentManager fm, List<? extends Fragment> frags) {
        mFragmentManager = fm;
        addFragment(frags);
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id");
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        Fragment nowFrag = mFragments.get(position);
        if (nowFrag.isAdded()) {
            if (nowFrag.isDetached()) mCurTransaction.attach(nowFrag);
        } else {
            mCurTransaction.add(container.getId(), nowFrag);
        }
        if (nowFrag != mCurrentPrimaryItem) {
            nowFrag.setMenuVisibility(false);
            nowFrag.setUserVisibleHint(false);
        }

        return nowFrag;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (mNoDestroyPosition == position) return;
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        mCurTransaction.remove((Fragment) object);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitNowAllowingStateLoss();
            mCurTransaction = null;
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public int getCount() {
        //返回Fragment的数量
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null && position >= 0 && position < mTitles.size())
            return mTitles.get(position);

        return null;
    }

    /**
     * 这个obj的新位置,一般notify时被调用
     * POSITION_UNCHANGED:永远都在原来的position(notify时不会{@link #destroyItem},也不会{@link #instantiateItem})
     * POSITION_NONE:没有位置(notify时会重新调两个方法来添加新视图)
     * >=0:挪到新的position了(对应的新position也不会走两个方法)
     */
    @Override
    public int getItemPosition(@NonNull Object object) {
        if (mNoDestroyPosition >= 0) {//>=0说明不想删除这个frag
            if (mFragments.get(mNoDestroyPosition) == object) return POSITION_UNCHANGED;
            return POSITION_NONE;
        }
        return mNoDestroyPosition;
    }

    /**
     * 调用这个和{@link BaseFragmentPagerAdapter}没什么区别，已经加载的frag不会发生任何变化
     * 请使用{@link #notifyAllItem}
     */
    @RequiresApi(999)
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 以下是自定义方法
    ///////////////////////////////////////////////////////////////////////////

    public BaseFragmentNotifyAdapter addFragment(Fragment... frags) {
        Collections.addAll(mFragments, frags);
        super.notifyDataSetChanged();//添加不会有问题
        return this;
    }

    public BaseFragmentNotifyAdapter addFragment(List<? extends Fragment> frags) {
        mFragments.addAll(frags);
        super.notifyDataSetChanged();//添加不会有问题
        return this;
    }

    /**
     * 删除相关操作请使用{@link #notifyAllItem}来解决白屏问题
     */
    public ArrayList<Fragment> getFragments() {
        return mFragments;
    }

    /**
     * 添加frag的title，类似TabLayout可能会用到
     */
    public BaseFragmentNotifyAdapter setTitles(ArrayList<? extends CharSequence> titles) {
        mTitles = titles;
        super.notifyDataSetChanged();
        return this;
    }

    /**
     * 这个方法会重新加载视图
     *
     * @param noDestroyPosition 不需要重新绑定的frag(你代码复用了这个frag)(删除再添加会白屏,保留一个不删除比较友好)
     */
    public void notifyAllItem(int noDestroyPosition) {
        if (noDestroyPosition >= 0) {
            mNoDestroyPosition = noDestroyPosition;
        } else {
            mNoDestroyPosition = POSITION_NONE;
        }
        super.notifyDataSetChanged();
        mNoDestroyPosition = POSITION_UNCHANGED;
        super.notifyDataSetChanged();
    }
}
