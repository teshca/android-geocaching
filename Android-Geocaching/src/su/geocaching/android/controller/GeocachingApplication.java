package su.geocaching.android.controller;

import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import android.app.Application;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since April 2011
 * 
 */
public class GeocachingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Controller.getInstance().setApplicationContext(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Controller.getInstance().onTerminate();
    }
}
