package su.geocaching.android.controller;

import android.util.Log;

/**
 * Manager for logging
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Mar 8, 2011
 */
public class LogManager {

    public static void d(String tag, String msg) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable ex) {
        if (Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, msg, ex);
        }
    }

    public static void e(String tag, String msg, Throwable ex) {
        if (Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, msg, ex);
        }
    }

}
