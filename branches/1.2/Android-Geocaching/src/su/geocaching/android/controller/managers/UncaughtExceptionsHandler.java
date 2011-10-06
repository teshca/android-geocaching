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
    
    public UncaughtExceptionsHandler() {
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Controller.getInstance().getGoogleAnalyticsManager().trackException("UncaughtException", thread.getName(), ex);
        defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
    }
}
