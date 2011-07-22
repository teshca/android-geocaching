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
        String stackTrace = ex.getMessage();
        for (StackTraceElement s : ex.getStackTrace()) {
            stackTrace += String.format(" at %s.%s(%s:%d)\n", s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber());
        }
        Controller.getInstance().getGoogleAnalyticsManager().trackEvent(thread.getName(), stackTrace, null, 0);
        defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
    }
}
