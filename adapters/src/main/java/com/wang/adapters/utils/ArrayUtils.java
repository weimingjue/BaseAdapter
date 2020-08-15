package com.wang.adapters.utils;

import java.util.Collection;

public class ArrayUtils {

    //集合是否是空的
    public static boolean isEmpty(Collection list) {
        return list == null || list.size() == 0;
    }

    public static <T> boolean isEmpty(T[] list) {
        return list == null || list.length == 0;
    }
}
