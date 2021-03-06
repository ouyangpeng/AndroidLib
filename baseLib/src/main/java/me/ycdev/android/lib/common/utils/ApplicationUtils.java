package me.ycdev.android.lib.common.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;

public class ApplicationUtils {
    private static final String TAG = "ApplicationUtils";

    @SuppressLint("StaticFieldLeak")
    private static Application sApp;
    private static Handler sHandler;
    private static String sProcessName;

    /**
     * Must be called in Application#onCreate() ASAP.
     */
    public static void initApplication(Application app) {
        sApp = app;
        sHandler = new Handler();
        getCurrentProcessName(); // init process name in UI thread
    }

    public static Context getApplicationContext() {
        return sApp;
    }

    public static String getCurrentProcessName() {
        if (!TextUtils.isEmpty(sProcessName)) {
            return sProcessName;
        }

        // try AMS first
        int pid = Process.myPid();
        sProcessName = getProcessNameFromAMS(sApp, pid);
        if (!TextUtils.isEmpty(sProcessName)) {
            return sProcessName;
        }

        // try "/proc"
        sProcessName = getProcessNameFromProc(pid);
        return sProcessName;
    }

    @Nullable
    private static String getProcessNameFromAMS(Context cxt, int pid) {
        ActivityManager am = SystemServiceHelper.getActivityManager(cxt);
        List<ActivityManager.RunningAppProcessInfo> runningApps =
                SystemServiceHelper.getRunningAppProcesses(am);
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    @Nullable
    private static String getProcessNameFromProc(int pid) {
        String processName = null;
        try {
            String cmdlineFile = "/proc/" + pid + "/cmdline";
            processName = IoUtils.readAllLines(cmdlineFile);
        } catch (IOException e) {
            LibLogger.w(TAG, "failed to read process name from /proc for pid [%d]", pid);
        }
        if (processName != null) {
            processName = processName.trim();
        }
        return processName;
    }

    public static void post(Runnable r) {
        sHandler.post(r);
    }

    public static void postDelayed(Runnable r, long delayMs) {
        sHandler.postDelayed(r, delayMs);
    }
}
