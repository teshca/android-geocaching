package su.geocaching.android.controller.managers;

import su.geocaching.android.controller.Controller;
import android.content.Context;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import su.geocaching.android.ui.R;

public class GoogleAnalyticsManager {
    private GoogleAnalyticsTracker tracker;
    private String applicationVersionName;

    public GoogleAnalyticsManager(Context context) {
        applicationVersionName = Controller.getInstance().getApplicationVersionName();
        String analyticsProfileId = Controller.DEBUG ? "UA-20327116-6" : "UA-20327116-3";
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.setProductVersion(context.getString(R.string.app_name), applicationVersionName);
        tracker.start(analyticsProfileId, context);
    }

    public void trackActivityLaunch(String activityName) {
        tracker.trackPageView(activityName);
        tracker.dispatch();
    }

    public void trackExternalActivityLaunch(String activityName) {
        tracker.trackPageView("/external" + activityName);
        tracker.dispatch();
    }

    public void trackCaughtException(String tag, Throwable ex) {
        trackException("Caught exception", tag, ex);
    }

    public void trackUncaughtException(String tag, Throwable ex) {
        trackException("Uncaught exception", tag, ex);
    }

    public void trackError(String tag, String message) {
        tracker.trackEvent("Error message: " + applicationVersionName, tag, message, 0);
        tracker.dispatch();
    }

    private void trackException(String category, String tag, Throwable ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "";
        StringBuilder stackTrace = new StringBuilder(message);
        final String NEW_LINE = " _____ ";
        for (StackTraceElement s : ex.getStackTrace()) {
            stackTrace.append(String.format(" at %s.%s(%s:%d)", s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber()));
            stackTrace.append(NEW_LINE);
        }
        tracker.trackEvent(category + ": " + applicationVersionName, tag, stackTrace.toString(), 0);
        tracker.dispatch();
    }
}
