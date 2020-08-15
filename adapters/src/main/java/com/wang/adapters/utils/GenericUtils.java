package com.wang.adapters.utils;

import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 获取泛型相关操作
 */
public class GenericUtils {
    private static final StringBuilder mBuilder = new StringBuilder();

    /**
     * 根据dataBinding的名称获取对应资源id
     * <p>
     * 使用条件：您必须不混淆{@link ViewDataBinding}的子类
     *
     * @param baseClass  基类class（BaseXxx.class）
     * @param childClass 子类class（getClass()）
     * @throws IndexOutOfBoundsException 必须在Proguard里忽略ViewDataBinding的子类不然会崩溃
     */
    @MainThread
    @LayoutRes
    public static int getGenericRes(Context context, Class baseClass, Class childClass) {
        Class<? extends ViewDataBinding> dbClass = GenericUtils.getGenericClass(ViewDataBinding.class, baseClass, childClass);
        if (dbClass == null || dbClass == ViewDataBinding.class) {
            throw new RuntimeException("泛型不合规：" + dbClass + "（如果想自定义，你必须覆盖相关方法）");
        }
        char[] chars = dbClass.getSimpleName().toCharArray();
        mBuilder.setLength(0);
        for (char c : chars) {
            if (c < 91) {
                c = (char) (c + 32);
                if (mBuilder.length() > 0) {
                    mBuilder.append("_");
                    mBuilder.append(c);
                } else {
                    mBuilder.append(c);
                }
            } else {
                mBuilder.append(c);
            }
        }
        mBuilder.setLength(mBuilder.length() - 8);//去掉结尾的_binding
        return context.getResources().getIdentifier(mBuilder.toString(), "layout", context.getPackageName());
    }

    /**
     * 获取泛型对应的class
     *
     * @param genericSuperClass 泛型父类的class，以便找到泛型
     * @param endClass          遍历结束的class（一般是最后一个泛型类的class，如BaseActivity.class）
     * @param myClass           当前类的class（getClass()）
     * @return 泛型的class
     */
    public static <T> Class<T> getGenericClass(@NonNull Class genericSuperClass, Class endClass, Class myClass) {
        if (myClass == endClass || myClass == Object.class || myClass == null) {
            return null;
        }
        Type superType = myClass.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) superType).getActualTypeArguments();
            for (Type type : types) {
                if (type instanceof Class) {
                    Class vhClass = (Class) type;
                    //noinspection unchecked
                    if (genericSuperClass.isAssignableFrom(vhClass)) {
                        //noinspection unchecked
                        return vhClass;
                    }
                }
            }
        }
        return getGenericClass(genericSuperClass, endClass, myClass.getSuperclass());
    }
}
