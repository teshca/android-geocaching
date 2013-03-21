package su.geocaching.android.controller.managers;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;

public class GoogleAnalyticsManager {

    public GoogleAnalyticsManager(Context context) {
        EasyTracker.getInstance().setContext(context);
    }

    public void trackActivityLaunch(String activityName) {
        EasyTracker.getTracker().sendView(activityName);
    }

    public void trackExternalActivityLaunch(String activityName) {
        EasyTracker.getTracker().sendView("/external" + activityName);
    }

    public void trackCaughtException(String tag, Throwable ex) {
        EasyTracker.getTracker().sendException(tag, ex, false);
    }

    public void trackUncaughtException(String tag, Throwable ex) {
        EasyTracker.getTracker().sendException(tag, ex, true);
    }

    public void trackError(String tag, String message) {
        EasyTracker.getTracker().sendException(String.format("Error in class %s : %s", tag, message), false);
    }
}
