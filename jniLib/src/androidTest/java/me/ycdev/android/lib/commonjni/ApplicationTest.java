package me.ycdev.android.lib.commonjni;

import android.app.Application;
import android.test.ApplicationTestCase;

import me.ycdev.android.lib.common.utils.LibLogger;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = "ApplicationTest";

    public ApplicationTest() {
        super(Application.class);
        LibLogger.d(TAG, "ctor");
    }
}