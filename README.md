# 代码非常简单，基于dataBinding

### 概览
本项目主要做了RecyclerView adapter、ListView adapter、ViewPager adapter fragment的封装，旨在减少大量模板代码。

也加了和这些相关连带问题的解决方法：adapter套RecyclerView回调繁琐、动态fragment刷新白屏、ViewPager的Transformer不能获取position、ViewPager ViewPager2高度不能wrap等问题的解决

### 详细介绍
一个简单的listAdapter只需要如下一行（没看错，总共就一行）
```
   BaseAdapterRvList<?, String> adapter = BaseAdapterRvList.createAdapter(R.layout.adapter_main_list);
```
当然，你的xml是基于dataBinding的
```
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <data>

        <!--        adapter默认会设置并刷新“bean”这个属性-->
        <variable
            name="bean"
            type="String" />
    </data>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#fff"
        android:padding="10dp"
        android:text="@{bean}"
        android:textColor="#333"
        android:textSize="20sp"
        tools:text="这是文本" />
</layout>
```
自带点击事件
```
adapter.setOnItemClickListener(new OnItemClickListener() {
    @Override
    public void onItemClick(@NonNull View view, int listPosition) {
    }

    @Override
    public boolean onItemLongClick(@NonNull View view, int listPosition) {
        return true;
    }

    @Override
    protected void onFooterClick(@NonNull View view) {
        super.onFooterClick(view);
    }
    //...header、footer long click
});
//只有onItemClick也可以使用语法糖：
adapter.setOnItemClickListener((view, listPosition) -> toast("你点击了：" + listPosition));
```
自带header、footer
```
adapter.setHeaderView(context, R.layout.adapter_main_header);//根布局可以使用height、layout_margin、layout_gravity相关属性
adapter.setFooterView(view);
```
当然你也可以自定义一些简单逻辑
```
BaseAdapterRvList<AdapterMainListBinding, String> adapter = BaseAdapterRvList.createAdapter(list, R.layout.adapter_main_list,
        (holder, listPosition, s) -> {
            if (s.contains("10")) {
                holder.itemView.setBackgroundColor(0xff999999);
            }
        });//回调还有onViewHolderCreated方法
```
也可以完全自定义（没看错，不需要layoutId）（适用于复杂逻辑，简单逻辑推荐使用上面方式）
```
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
```
adapter里有个button，点完后还要写个回调给Activity？？？反正我是不喜欢：
```
public void onBindListViewHolder(@NonNull BaseViewHolder<AdapterMainListBinding> holder, int listPosition, TestBean bean) {
    setItemViewClick(holder.getBinding().btButton, holder);
}
...
adapter.setOnItemClickListener(new OnItemClickListener() {
    @Override
    public void onItemClick(@NonNull View view, int listPosition) {
        switch (view.getId()) {
            case R.id.bt_button:
                toast("没想到吧，还能这样玩：" + listPosition);
                break;
            default:
                toast("你点击了整个条目：" + listPosition);
                break;
        }
    }
});
```
adapter里又套了一个RecyclerView，简直是回调地狱啊...完全受不了：
```
public void onBindListViewHolder(@NonNull BaseViewHolder<AdapterMainListBinding> holder, int listPosition, TestBean bean) {
    //需提前setAdapter、layoutManager（在bind或者每次create时）
    setItemRvData(holder.getBinding().rvItemList, holder, bean.itemTextList);
}
...
adapter.setOnItemClickListener(new OnItemItemClickListener() {
    @Override
    public void onParentItemClick(@NonNull View view, int parentPosition) {
        toast("你点击了外层：" + parentPosition);
    }

    @Override
    public void onChildItemClick(@NonNull View view, int parentPosition, int childPosition) {
        toast("你点击了外层：" + parentPosition + "，内层：" + childPosition);
    }

    @Override
    public void onParentHeaderClick(@NonNull View view) {
        toast("你点击了外层：header");
    }

    @Override
    public void onChildHeaderClick(@NonNull View view, int parentPosition) {
        toast("你点击了外层：" + parentPosition + "，内层：header");
    }
    //...footer、longClick等都有
});
```
### 特殊情况
如果真的不想加混淆，则adapter的构造里传入layoutRes即可
```
public MyAdapter() {
    super(R.layout.adapter_main_list, null);
}
```
如果真的不想使用dataBinding，则覆盖onCreateListViewHolder方法，不调用super即可
```
@NonNull
@Override
public BaseViewHolder<ViewDataBinding> onCreateListViewHolder(@NonNull ViewGroup parent) {
    TextView tv = new AppCompatTextView(parent.getContext());
    tv.setTag(R.id.tag_view_no_data_binding, "");
    return new BaseViewHolder<>(tv);
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
PageTransformer不能获得view的position？手动写TAG+重写getItemPosition（这个方法的意义不多说吧）？简直无力吐槽：
```
//fragment的
int fragPosition = BaseFragmentPagerAdapter对象.getRootViewPosition(View rootView);
//普通adapter的
int adapterPosition = BaseAdapterLvs子类.getRootViewPosition(View rootView);
```

### 基于list的简单多条目
```
BaseAdapterRvMultipleList<TestBean> adapter = new BaseAdapterRvMultipleList<>();
//添加条目1
adapter.addMultipleItem(R.layout.adapter_main_multiple_0, new BaseAdapterRvMultipleList.OnMultipleListListener<AdapterMainMultiple0Binding, TestBean>() {
    @Override
    public boolean isThisType(@NonNull BaseAdapterRvMultipleList<TestBean> adapter, int listPosition, @NonNull TestBean bean) {
        return listPosition % 2 == 0;//true表示这条数据是这个条目的
    }

    @Override
    public void onBindListViewHolder(@NonNull BaseAdapterRvMultipleList<TestBean> adapter, @NonNull BaseViewHolder<AdapterMainMultiple0Binding> holder, int listPosition, @NonNull TestBean bean) {
        holder.getBinding().tvHhh.setTextColor(0xff00ff00);
    }
    //当然还有onCreateListViewHolder没写
})
//添加条目2，不需要bind直接使用语法糖
.addMultipleItem(R.layout.adapter_main_multiple_1, (adapter1, listPosition, bean) -> listPosition % 2 != 0);

adapter.setListAndNotifyDataSetChanged(list);
```
**多条目太复杂，无法满足？**

当然还有适用于各种复杂样式的adapter容器（如：聊天列表，首页、今日头条的列表等）：

本项目已默认导入，直接使用即可： [一个通过add其他adapter的超级容器，无论多么复杂的列表样式均可解耦成一个一个的adapter](https://github.com/weimingjue/BaseContainerAdapter)

简单示例（具体请看详情介绍）：
```
mRv.setLayoutManager(new LinearLayoutManager(this));//如果是GridLayoutManager需要提前设置好，Linear随意
BaseContainerAdapter baseAdapter = new BaseContainerAdapter();
mRv.setAdapter(baseAdapter.addAdapter(new TextAdapter(),new ImageAdapter()));
//...
baseAdapter.setListAndNotifyDataSetChanged(list);
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

有header、footer调用notifyItem...时请注意+1（Adapter的通病，无解决方案）

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
`（api或）implementation 'com.github.weimingjue:BaseAdapter:4.2.1'`

不需要layoutId的混淆要求：
```
# 框架特殊要求
# 根据泛型获取res资源需要
-keep class * extends androidx.databinding.ViewDataBinding
```