package com.qinggan.os;

import android.app.ActivityManager;
import android.content.Context;
import android.os.IBinder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceManager {
    private static final String TAG = "ServiceManager";
    private static Class mClass;
    private static Class mActivityManagerClass;

    static {
        try {
            mClass = Class.forName("android.os.ServiceManager");
            mActivityManagerClass = Class.forName("android.app.ActivityManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static IBinder getService(String name) {
        try {
            return (IBinder) mClass.getMethod("getService", String.class).invoke(null, name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setPersistent(Context context, String pkg, boolean persistent) throws Exception {
        try {
            mActivityManagerClass.getMethod("setPersistent", String.class, Boolean.TYPE)
                .invoke((ActivityManager) context.getSystemService("activity"), pkg, Boolean.valueOf(persistent));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
