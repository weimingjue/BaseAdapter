# 代码非常简单，基于dataBinding

### 概览

本项目主要做了RecyclerView adapter、ListView adapter、ViewPager adapter fragment的封装，旨在减少大量模板代码。

也加了和这些相关连带问题的解决方法：adapter套RecyclerView回调繁琐、动态fragment刷新白屏、ViewPager和ViewPager2高度不能wrap等问题的解决

### 背景介绍

Adapter属于View层，Activity也是View层，在当前各种新框架下两个几乎没有什么代码，然而它们之间交互起来特别繁琐，如果我们直接将Adapter合并到Activity里，会产生什么意想不到的结果呢？

### 详细介绍

一个简单的listAdapter只需要如下一行（没看错，总共就一行），基于ViewBinding

```
class MainActivity : AppCompatActivity() {
    private val listAdapter by lazyNone {
        createListVbAdapter<AdapterMainListBinding, String> { holder, vb, bean -> }
    }
}
```

点击事件什么的都不需要额外操作，没有回调也没有额外传参，直接在Adapter写就行了

```
class MainActivity : AppCompatActivity() {
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
        //自带header、footer
            headerView = AppCompatTextView(this@MainActivity).apply {
                text = "这是header"
            }
        }
    }
}
```

### ViewPager的Fragment更简单

```
mVp.setAdapter(new BaseFragmentPagerAdapter(getSupportFragmentManager(), mFrags));
//或
mVp.setAdapter(new BaseFragmentPagerAdapter(getSupportFragmentManager(), frag1,frag2...));
//动态修改frag
mAdapter = new BaseFragmentStatePagerAdapter(getSupportFragmentManager(), mFrags);
mVp.setAdapter(mAdapter);
...
mAdapter.getFragments().add(xxx);//由于内部有新的list，所以并不能用自己的mFrags
mAdapter.getFragments().remove(yyy);
mAdapter.notifyDataSetChanged();
//解决动态修改刷新白屏的问题
BaseFragmentNotifyAdapter adapter = new BaseFragmentNotifyAdapter(getSupportFragmentManager(), mFrags);
mVp.setAdapter(adapter);
...
adapter.notifyAllItem(1);//保留展示的frag这样就不会白屏了，想要刷新这个frag当然需要自己frag内部刷新了，详见app下的示例
```

### 更精简的条目写法

只有2个方法：addMultipleItem、addDefaultMultipleItem
addMultipleItem的isThisTypeCallback返回一个布尔值表示是否是当前条目，不需要额外其他逻辑

```
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
```

### 嵌套？直接正常写就行了

注意嵌套效率问题，不在当前讨论范围内

```
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
```

**多条目太复杂，无法满足？**

当然还有适用于各种复杂样式的adapter容器（如：聊天列表，首页、今日头条的列表等）：

本项目已默认导入，直接使用即可： [一个通过add其他adapter的超级容器，无论多么复杂的列表样式均可解耦成一个一个的adapter](https://github.com/weimingjue/BaseContainerAdapter)

简单示例（具体请看详情介绍）：

```
mRv.setLayoutManager(LinearLayoutManager(this))//如果是GridLayoutManager需要提前设置好，Linear随意
val baseAdapter = createContainerAdapter()
mRv.setAdapter(baseAdapter.addAdapter(TextAdapter(),ImageAdapter()))
//...
baseAdapter.setListAndNotifyDataSetChanged(list)
```

### ViewPager、RecyclerView、ViewPager2高度自适应第一个child

```
<androidx.viewpager2.widget.ViewPager2
    android:id="@+id/vp_pager"
    android:layout_width="match_parent"
    android:layout_height="1dp"/>//随便写一个高

//初始化时即可调用（如果child高度因数据再次变化，这里不会更新，请在合适时机再次调用，后续可能会进行优化）
ViewGroupWrapUtils.wrap(vp, false);
```

## 使用小贴士

本项目所有的adapter都会内部维护一个List，所以修改数据请一定要使用adapter.getList()，然后notify...

刷新list数据建议调用notifyListItem...方法，这样就不用处理header、footer数量了

关于增加多个header、footer：个人认为多个header、footer场景少并且双方都难以管理，所以用到时请自己写个LinearLayout

关于空状态：个人认为这不在adapter范畴（对上拉下拉、notifyItem都不太友好），自行写个空状态工具类反而更方便（很简单，如有需要后续开放）

## 导入方式

你的build.gradle要有jitpack.io，大致如下

```
allprojects {
    repositories {
        maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
        maven { url 'https://jitpack.io' }
        google()
        jcenter()
    }
}
```

然后：
`（api或）implementation 'com.github.weimingjue:BaseAdapter:4.3.0'`

混淆要求：
请保留ViewBinding里的两个inflate方法，不然会反射不到