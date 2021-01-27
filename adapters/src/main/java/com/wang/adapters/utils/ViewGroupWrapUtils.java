package com.wang.adapters.utils;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

public class ViewGroupWrapUtils {

    /**
     * 获得child的宽高
     */
    public static void getChildWidthHeight(ViewGroup vg, @NonNull OnChildWidthHeightResultListener listener) {
        vg.post(() -> {
            if (vg.getChildCount() > 0 && (vg.getChildAt(0).getWidth() > 0 || vg.getChildAt(0).getHeight() > 0)) {
                View childAt = vg.getChildAt(0);
                listener.onResult(childAt.getWidth(), childAt.getHeight());
            } else {
                vg.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (vg.getChildCount() > 0) {
                            View childAt = vg.getChildAt(0);
                            if (childAt.getWidth() > 0 || childAt.getHeight() > 0) {
                                vg.removeOnLayoutChangeListener(this);
                                listener.onResult(childAt.getWidth(), childAt.getHeight());
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 将vp或rv的高/宽与第一个child高/宽一致（相当于wrap）
     *
     * @param isWidth 修改宽还是高
     */
    public static void wrap(RecyclerView rv, boolean isWidth) {
        rv.post(() -> {
            if (rv.getChildCount() > 0) {
                setMeasureSize(rv, rv.getChildAt(0), isWidth);
            } else {
                rv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                    @Override
                    public void onChildViewAttachedToWindow(@NonNull View view) {
                        if (rv.getChildCount() > 0) {
                            rv.removeOnChildAttachStateChangeListener(this);
                            setMeasureSize(rv, rv.getChildAt(0), isWidth);
                        }
                    }

                    @Override
                    public void onChildViewDetachedFromWindow(@NonNull View view) {
                    }
                });
            }
        });
    }

    /**
     * 将vp或rv的高/宽与第一个child高/宽一致（相当于wrap）
     *
     * @param isWidth 修改宽还是高
     */
    public static void wrap(ViewPager vp, boolean isWidth) {
        vp.post(() -> {
            if (vp.getChildCount() > 0) {
                setMeasureSize(vp, vp.getChildAt(0), isWidth);
            } else {
                vp.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (vp.getChildCount() > 0) {
                            vp.removeOnLayoutChangeListener(this);
                            setMeasureSize(vp, vp.getChildAt(0), isWidth);
                        }
                    }
                });
            }
        });
    }

    /**
     * 将vp或rv的高/宽与第一个child高/宽一致（相当于wrap）
     *
     * @param isWidth 修改宽还是高
     */
    public static void wrap(ViewPager2 vp, boolean isWidth) {
        RecyclerView rv = (RecyclerView) vp.getChildAt(0);
        rv.post(() -> {
            if (rv.getChildCount() > 0) {
                setMeasureSize(vp, rv.getChildAt(0), isWidth);
            } else {
                rv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                    @Override
                    public void onChildViewAttachedToWindow(@NonNull View view) {
                        if (rv.getChildCount() > 0) {
                            rv.removeOnChildAttachStateChangeListener(this);
                            setMeasureSize(vp, rv.getChildAt(0), isWidth);
                        }
                    }

                    @Override
                    public void onChildViewDetachedFromWindow(@NonNull View view) {
                    }
                });
            }
        });
    }

    private static void setMeasureSize(ViewGroup vg, View child, boolean isWidth) {
        child.post(() -> {
            DisplayMetrics dm = vg.getResources().getDisplayMetrics();
            if (isWidth) {
                int screenWidth = dm.widthPixels;
                child.measure(View.MeasureSpec.makeMeasureSpec(screenWidth * 5, View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), View.MeasureSpec.EXACTLY));

                vg.getLayoutParams().width = child.getMeasuredWidth() + vg.getPaddingLeft() + vg.getPaddingRight();
            } else {
                int screenHeight = dm.heightPixels;
                child.measure(View.MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(screenHeight * 5, View.MeasureSpec.AT_MOST));

                vg.getLayoutParams().height = child.getMeasuredHeight() + vg.getPaddingTop() + vg.getPaddingBottom();
            }
            vg.setLayoutParams(vg.getLayoutParams());
        });
    }

    public interface OnChildWidthHeightResultListener {
        void onResult(int width, int height);
    }
}
