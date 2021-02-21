package com.jhony.withwhu.myutils;

import android.content.Context;
import android.preference.PreferenceManager;

public class MyPreferences {
    public static final String HAS_DATAS = "hasDatas";//是否含有成绩信息
    public static final String USER = "user";//学号
    public static final String PASSWORD = "password";//密码

    //设置是否含有成绩标志位
    public static void setHasDatas(Context context, boolean hasDatas){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(HAS_DATAS, hasDatas)
                .apply();
    }

    //获取标志位
    public static boolean getHasDatas(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(HAS_DATAS, false);
    }

    //一般字符串设置
    private static void setCommonString(Context context, String key, String value){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key, value)
                .apply();
    }

    //一般字符串读取
    private static String getCommonString(Context context, String key){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, null);
    }

    //设置学号
    public static void setUser(Context context, String user){
        setCommonString(context, USER, user);
    }
    //设置密码
    public static void setPassword(Context context, String password){
        setCommonString(context, PASSWORD, password);
    }

    //获取学号
    public static String getUser(Context context){
        return getCommonString(context, USER);
    }
    //获取密码
    public static String getPassword(Context context){
        return getCommonString(context, PASSWORD);
    }
}
