package com.wang.adapters.utils

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

object ViewGroupWrapUtils {
    /**
     * 获得child的宽高
     */
    fun getChildWidthHeight(vg: ViewGroup, listener: OnChildWidthHeightResultListener) {
        vg.post {
            if (vg.childCount > 0 && (vg.getChildAt(0).width > 0 || vg.getChildAt(0).height > 0)) {
                val childAt = vg.getChildAt(0)
                listener.onResult(childAt.width, childAt.height)
            } else {
                vg.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                        if (vg.childCount > 0) {
                            val childAt = vg.getChildAt(0)
                            if (childAt.width > 0 || childAt.height > 0) {
                                vg.removeOnLayoutChangeListener(this)
                                listener.onResult(childAt.width, childAt.height)
                            }
                        }
                    }
                })
            }
        }
    }

    /**
     * 将vp或rv的高/宽与第一个child高/宽一致（相当于wrap）
     *
     * @param isWidth 修改宽还是高
     */
    fun wrap(rv: RecyclerView, isWidth: Boolean) {
        rv.post {
            if (rv.childCount > 0) {
                setMeasureSize(rv, rv.getChildAt(0), isWidth)
            } else {
                rv.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                    override fun onChildViewAttachedToWindow(view: View) {
                        if (rv.childCount > 0) {
                            rv.removeOnChildAttachStateChangeListener(this)
                            setMeasureSize(rv, rv.getChildAt(0), isWidth)
                        }
                    }

                    override fun onChildViewDetachedFromWindow(view: View) {}
                })
            }
        }
    }

    /**
     * 将vp或rv的高/宽与第一个child高/宽一致（相当于wrap）
     *
     * @param isWidth 修改宽还是高
     */
    fun wrap(vp: ViewPager, isWidth: Boolean) {
        vp.post {
            if (vp.childCount > 0) {
                setMeasureSize(vp, vp.getChildAt(0), isWidth)
            } else {
                vp.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                        if (vp.childCount > 0) {
                            vp.removeOnLayoutChangeListener(this)
                            setMeasureSize(vp, vp.getChildAt(0), isWidth)
                        }
                    }
                })
            }
        }
    }

    /**
     * 将vp或rv的高/宽与第一个child高/宽一致（相当于wrap）
     *
     * @param isWidth 修改宽还是高
     */
    fun wrap(vp: ViewPager2, isWidth: Boolean) {
        val rv = vp.getChildAt(0) as RecyclerView
        rv.post {
            if (rv.childCount > 0) {
                setMeasureSize(vp, rv.getChildAt(0), isWidth)
            } else {
                rv.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                    override fun onChildViewAttachedToWindow(view: View) {
                        if (rv.childCount > 0) {
                            rv.removeOnChildAttachStateChangeListener(this)
                            setMeasureSize(vp, rv.getChildAt(0), isWidth)
                        }
                    }

                    override fun onChildViewDetachedFromWindow(view: View) {}
                })
            }
        }
    }

    private fun setMeasureSize(vg: ViewGroup, child: View, isWidth: Boolean) {
        child.post {
            val dm = vg.resources.displayMetrics
            if (isWidth) {
                val screenWidth = dm.widthPixels
                child.measure(
                    View.MeasureSpec.makeMeasureSpec(screenWidth * 5, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(child.measuredHeight, View.MeasureSpec.EXACTLY)
                )
                vg.layoutParams.width = child.measuredWidth + vg.paddingLeft + vg.paddingRight
            } else {
                val screenHeight = dm.heightPixels
                child.measure(
                    View.MeasureSpec.makeMeasureSpec(child.measuredWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(screenHeight * 5, View.MeasureSpec.AT_MOST)
                )
                vg.layoutParams.height = child.measuredHeight + vg.paddingTop + vg.paddingBottom
            }
            vg.layoutParams = vg.layoutParams
        }
    }
}