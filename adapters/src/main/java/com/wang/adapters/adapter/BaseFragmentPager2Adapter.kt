package com.wang.adapters.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

/**
 * [ViewPager2] fragment adapter的终极封装
 */
class BaseFragmentPager2Adapter(fm: FragmentManager, lifecycle: Lifecycle, vararg frags: Fragment) :
    FragmentStateAdapter(fm, lifecycle) {

    constructor(frag: Fragment, vararg frags: Fragment) :
            this(frag.childFragmentManager, frag.lifecycle, *frags)

    constructor(act: AppCompatActivity, vararg frags: Fragment) :
            this(act.supportFragmentManager, act.lifecycle, *frags)

//    constructor(ui: IUIContext, vararg frags: Fragment) :
//            this(ui.currentFragmentManager, ui.lifecycle, *frags)

    var fragList = frags.toMutableList()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = fragList.size

    override fun createFragment(position: Int): Fragment = fragList[position]

}