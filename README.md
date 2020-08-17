# 代码非常简单，基于dataBinding

## 详细示例见本项目app下的MainActivity
### 说明：adapter的功能、方向及逻辑已经明确，后续将稳定发版，不会有较大改动

一个listAdapter只需要如下一行（没错，总共就一行）
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
```
自带header、footer
```
adapter.setHeaderView(view);
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
也可以完全自定义（没有看错，不需要layoutId）
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
ViewPager的Fragment更简单
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
还有适用于各种复杂样式的adapter容器（如：聊天列表，首页、今日头条的列表等）：
```
本项目已默认导入，直接使用即可：https://github.com/weimingjue/BaseContainerAdapter
```

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

### AndroidX dataBinding：
`（api或）implementation 'com.github.weimingjue:BaseAdapter:4.0.2'`

不需要layoutId的混淆要求：
```
#框架特殊要求
# 根据泛型获取res资源需要
-keep class * extends androidx.databinding.ViewDataBinding
```