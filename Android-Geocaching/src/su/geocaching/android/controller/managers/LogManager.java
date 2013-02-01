package su.geocaching.android.controller.managers;

import android.util.Log;
import su.geocaching.android.controller.Controller;

/**
 * Manager for logging
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class LogManager {
    private static final boolean DEBUG = Controller.DEBUG; // it is constant really need, because compiler can remove code blocks which cannot be execute

    /**
     * Send a DEBUG log message.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg
     *         The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msgFormat
     *         Formatted message you would like logged.
     * @param params
     *         Params for formatted string
     */
    public static void d(String tag, String msgFormat, Object... params) {
        d(tag, String.format(msgFormat, params));
    }

    /**
     * Send a INFO log message.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg
     *         The message you would like logged.
     */
    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    /**
     * Send a ERROR log message.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg
     *         The message you would like logged.
     */
    public static void e(String tag, String msg) {
        Controller.getInstance().getGoogleAnalyticsManager().trackError(tag, msg);
        Log.e(tag, msg);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg
     *         The message you would like logged.
     */
    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    /**
     * Send a WARNING log message.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg
     *         The message you would like logged.
     */
    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    /**
     * Send a WARNING log message and log the exception.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg
     *         The message you would like logged.
     * @param ex
     *         An exception to log
     */
    public static void w(String tag, String msg, Throwable ex) {
        Log.w(tag, msg, ex);
    }

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param tag
     *         Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg
     *         The message you would like logged.
     * @param ex
     *         An exception to log
     */
    public static void e(String tag, String msg, Throwable ex) {
        Controller.getInstance().getGoogleAnalyticsManager().trackCaughtException(tag, ex);
        Log.e(tag, msg, ex);
    }

    public static void e(String tag, Throwable ex) {
        Controller.getInstance().getGoogleAnalyticsManager().trackCaughtException(tag, ex);
        Log.e(tag, ex.getMessage(), ex);
    }
}
