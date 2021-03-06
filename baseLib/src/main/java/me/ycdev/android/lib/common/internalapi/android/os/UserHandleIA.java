package me.ycdev.android.lib.common.internalapi.android.os;

import android.os.Build;
import android.support.annotation.RestrictTo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.ycdev.android.lib.common.utils.LibConfigs;
import me.ycdev.android.lib.common.utils.LibLogger;

public class UserHandleIA {
    private static final String TAG = "UserHandleIA";
    private static final boolean DEBUG = LibConfigs.DEBUG_LOG;

    private static Class<?> sClass_UserHandle;
    private static Method sMtd_myUserId;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                // Android 4.1
                // There are both "UserId" and "UserHandle" in "Xiaomi MI 2SC, MIUI V5-3, Android 4.1.1"
                sClass_UserHandle = Class.forName("android.os.UserId");
            } catch (ClassNotFoundException e) {
                try {
                    // Android 4.2 ~ ?
                    sClass_UserHandle = Class.forName("android.os.UserHandle");
                } catch (ClassNotFoundException e1) {
                    if (DEBUG) LibLogger.w(TAG, "class not found", e1);
                }
            }

            if (sClass_UserHandle != null) {
                try {
                    sMtd_myUserId = sClass_UserHandle.getMethod("myUserId");
                } catch (NoSuchMethodException e) {
                    if (DEBUG) LibLogger.w(TAG, "method not found", e);
                }
            }
        }
    }

    private UserHandleIA() {
        // nothing to do
    }

    public static int myUserId() {
        if (sMtd_myUserId != null) {
            try {
                return (Integer) sMtd_myUserId.invoke(null);
            } catch (IllegalAccessException e) {
                if (DEBUG) LibLogger.w(TAG, "Failed to invoke #myUserId()", e);
            } catch (InvocationTargetException e) {
                if (DEBUG) LibLogger.w(TAG, "Failed to invoke #myUserId() more", e);
            }
        }
        return 0;
    }

    /**
     * Just for unit test.
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    static boolean checkReflect_myUserId() {
        return sMtd_myUserId != null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN;
    }

}
