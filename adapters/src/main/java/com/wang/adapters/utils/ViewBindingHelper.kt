package com.wang.adapters.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
object ViewBindingHelper {
    /**
     * 缓存已找到的class
     * viewBinding是有限的，所以不需要清
     */
    private val cacheClass = HashMap<String, Class<out ViewBinding>>(64)
    private val cacheInflateMethod = HashMap<Class<out ViewBinding>, Method>(64)

    /**
     * 获取ViewBinding的class
     *
     * @param myClass 当前类的class（getClass()）
     */
    @MainThread
    fun <T : ViewBinding> getViewBindingClass(myClass: Class<*>?): Class<T>? {
        if (myClass == Any::class.java || myClass == null) {
            return null
        }
        val cacheKey = myClass.name
        cacheClass[cacheKey]?.let {
            return it as Class<T>
        }
        //遍历父类所有泛型参数
        (myClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.forEach { type ->
            if (type is Class<*>) {
                if (ViewBinding::class.java.isAssignableFrom(type)) {
                    val clazz = type as Class<T>
                    cacheClass[cacheKey] = clazz//缓存
                    return clazz
                }
            }
        }
        //继续循环父类查询
        return getViewBindingClass(myClass.superclass)
    }

    /**
     * 获取ViewBinding的class，直接指定泛型位置
     *
     * @param myClass 当前类的class（getClass()）
     * @param typeIndex 泛型的位置，如：你的泛型在第二个位置传1
     */
    @MainThread
    fun <T : ViewBinding> getViewBindingClassFromIndex(
        myClass: Class<*>?,
        typeIndex: Int
    ): Class<T>? {
        if (myClass == Any::class.java || myClass == null) {
            return null
        }
        val cacheKey = "${myClass.name}#$typeIndex"
        cacheClass[cacheKey]?.let {
            return it as Class<T>
        }
        //直接取对应index的泛型参数
        ((myClass.genericSuperclass as? ParameterizedType)
            ?.actualTypeArguments
            ?.getOrNull(typeIndex) as? Class<*>)
            ?.let {
                if (ViewBinding::class.java.isAssignableFrom(it)) {
                    val clazz = it as Class<T>
                    cacheClass[cacheKey] = clazz//缓存
                    return clazz
                }
            }
        //继续循环父类查询
        return getViewBindingClassFromIndex(myClass.superclass, typeIndex)
    }

    /**
     * 根据类获取vb实例
     * @param obj 当前带ViewBinding泛型的实例类
     */
    @MainThread
    fun <T : ViewBinding> getViewBindingInstance(
        obj: Any,
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean = false
    ): T {
        val bindingCls: Class<T> = getViewBindingClass(obj.javaClass)
            ?: throw IllegalArgumentException("没有找到类${obj}的ViewBinding，请检查")
        return getViewBindingInstanceByClass(bindingCls, layoutInflater, container, attachToParent)
    }

    /**
     * 根据vb class获取vb实例
     */
    @MainThread
    fun <T : ViewBinding> getViewBindingInstanceByClass(
        clz: Class<out T>,
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean = false
    ): T {
        try {
            val method = cacheInflateMethod.getOrPut(clz) {
                clz.getDeclaredMethod(
                    "inflate",
                    LayoutInflater::class.java,
                    ViewGroup::class.java,
                    Boolean::class.java
                )
            }
            val vb = method.invoke(null, layoutInflater, container, attachToParent) as T
            //保存自身，方便其他框架使用
            vb.saveVbToTag()
            return vb
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("无法实例化${clz}，请注意是否开启了ViewBinding.inflate混淆", e)
        }
    }

    /**
     * inline实现
     */
    inline fun <reified T : ViewBinding> getViewBindingInstanceByClass(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean = false
    ) = getViewBindingInstanceByClass(T::class.java, layoutInflater, container, attachToParent)
}