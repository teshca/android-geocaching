package su.geocaching.android.controller.managers;

import su.geocaching.android.controller.Controller;

/**
 * Catch uncaughted exceptions and report it to Google Analytics Manager. You should create new instance
 * for every thread
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Jul 18, 2011
 * 
 */
public class UncaughtExceptionsHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    private static final String TAG = UncaughtExceptionsHandler.class.getCanonicalName();
    
    public UncaughtExceptionsHandler() {
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try
        {
           Controller.getInstance().getGoogleAnalyticsManager().trackUncaughtException("Thread name: " + thread.getName(), ex);
        }
        catch (Exception e)
        {
            // Prevent infinite exception loop
            android.util.Log.e(TAG, "Exception while trying to report uncaught exception");
        }
        defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
    }
}
