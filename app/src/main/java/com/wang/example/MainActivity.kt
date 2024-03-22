package com.wang.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.wang.adapters.adapter.BaseAdapter
import com.wang.adapters.utils.adapterLayoutPosition
import com.wang.adapters.utils.createListVbAdapter
import com.wang.adapters.utils.createMultiAdapter
import com.wang.adapters.utils.getAdapterOrCreate
import com.wang.adapters.utils.listPosition
import com.wang.adapters.utils.setGridLayoutManagerSpanSizeLookup
import com.wang.adapters.utils.setOnFastClickListener
import com.wang.example.databinding.ActivityMainBinding
import com.wang.example.databinding.AdapterMainListBinding
import com.wang.example.databinding.AdapterMainMultiple0Binding
import com.wang.example.databinding.AdapterMainMultiple1Binding
import com.wang.example.databinding.AdapterMainMultipleDefBinding
import com.wang.example.databinding.AdapterMainNesBinding
import com.wang.example.databinding.AdapterMainNesItemBinding

class MainActivity : AppCompatActivity() {

    /**
     * 无锁不支持并发的，适用于单线程
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> lazyNone(noinline initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

    private val listAdapter by lazyNone {
        createListVbAdapter<AdapterMainListBinding, String> { holder, vb, bean ->
            if (bean.contains("10")) {
                holder.itemView.setBackgroundColor(0xff999999.toInt())
            }
            vb.tvText.text = bean
            vb.btButton.setOnFastClickListener {
                toast("点击了Button")
            }
            holder.setOnFastClickListener {
                toast("点击item了${holder.listPosition}")
            }
        }.apply {
            headerView = AppCompatTextView(this@MainActivity).apply {
                text = "这是header"
            }
        }
    }

    private val multiAdapter by lazyNone {
        createMultiAdapter<TestBean>().apply {
            addMultipleItem<AdapterMainMultiple0Binding>(isThisTypeCallback = { listPosition, _ -> listPosition % 3 == 0 }) { holder, vb, bean ->
                vb.tvText.setTextColor(0xff00ff00.toInt())
                vb.tvText.text = "多条目0：${bean.text}"
                holder.setOnFastClickListener {
                    toast("点击item了${holder.listPosition}")
                }
            }
            addMultipleItem<AdapterMainMultiple1Binding>(isThisTypeCallback = { listPosition, _ -> listPosition % 3 == 1 }) { holder, vb, bean ->
                vb.tvText2.text = "多条目1：${bean.text}"
            }
            addDefaultMultipleItem<AdapterMainMultipleDefBinding>()

            headerView = AppCompatTextView(this@MainActivity).apply { text = "这是header" }
            footerView = AppCompatTextView(this@MainActivity).apply { text = "这是footer" }
        }
    }

    private val nesAdapter by lazyNone {
        createListVbAdapter<AdapterMainNesBinding, TestBean> { holder, vb, bean ->
            vb.tvNesText.text = "这是嵌套外层：${bean.text},${holder.listPosition},${holder.adapterLayoutPosition}"
            val itemAdapter = vb.rvItemList.getAdapterOrCreate {
                createListVbAdapter<AdapterMainNesItemBinding, String> { holder, vb, bean ->
                    vb.tvItem.text = "这是内层$bean,${holder.listPosition},${holder.adapterLayoutPosition}"
                }.apply {
                    headerView = AppCompatTextView(this@MainActivity).apply {
                        text = "这是内层header"
                        setOnFastClickListener {
                            toast("你点击了内层header")
                        }
                    }
                    footerView = AppCompatTextView(this@MainActivity).apply { text = "这是内层footer" }
                    vb.rvItemList.setGridLayoutManagerSpanSizeLookup {
                        when (it) {
                            0, (itemCount - 1) -> 2
                            else -> 1
                        }
                    }
                }
            }
            itemAdapter.notifyDataSetChanged(bean.itemTextList)
        }.apply {
            headerView = AppCompatTextView(this@MainActivity).apply {
                text = "这是外层header"
                setTextColor(0xff00ff00.toInt())
            }
        }
    }

    private var currentAdapter = 0
    private val adapters: Array<BaseAdapter> by lazyNone { arrayOf(listAdapter, multiAdapter, nesAdapter) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.tvChange.setOnFastClickListener {
            currentAdapter = (currentAdapter + 1) % adapters.size
            changeAdapter(vb)
        }

        vb.rvMain.layoutManager = LinearLayoutManager(this)
        val list = ArrayList<TestBean>()
        for (i in 0..99) {
            val tb = TestBean("第$i")
            if (i % 5 == 0) {
                for (j in 0..9) {
                    tb.itemTextList.add("第$i，子$j")
                }
            }
            list.add(tb)
        }
        listAdapter.notifyDataSetChanged(list.map { it.text })
        multiAdapter.notifyDataSetChanged(list)
        nesAdapter.notifyDataSetChanged(list)

        changeAdapter(vb)
    }

    private fun changeAdapter(vb: ActivityMainBinding) {
        vb.rvMain.adapter = adapters[currentAdapter]
    }

    class TestBean(
        val text: String,
        val itemTextList: ArrayList<String> = arrayListOf()
    )

    fun toast(st: String) {
        Toast.makeText(this, st, Toast.LENGTH_SHORT).show()
    }
}
