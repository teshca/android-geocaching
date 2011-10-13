package su.geocaching.android.controller.managers;

import su.geocaching.android.controller.Controller;
import android.content.Context;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GoogleAnalyticsManager {
    private static final int DISPATCH_INTERVAL = 120; // seconds
    private GoogleAnalyticsTracker tracker;
    private String applicationVersionName;

    public GoogleAnalyticsManager(Context context) {
        applicationVersionName = Controller.getInstance().getApplicationVersionName();
        String analyticsProfileId = Controller.DEBUG ? "UA-20327116-6" : "UA-20327116-3";
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(analyticsProfileId, DISPATCH_INTERVAL, context);
    }

    public void trackActivityLaunch(String activityName) {
        tracker.trackPageView(activityName);
    }

    public void trackExternalActivityLaunch(String activityName) {
        tracker.trackPageView("/external" + activityName);
    }

    public void trackException(String category, String action, Throwable ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "";
        StringBuilder stackTrace = new StringBuilder(message);
        final String NEW_LINE = "--//--";
        for (StackTraceElement s : ex.getStackTrace()) {
            stackTrace.append(String.format(" at %s.%s(%s:%d)", s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber()));
            stackTrace.append(NEW_LINE);
        }
        tracker.trackEvent(category + ":" + applicationVersionName, action, stackTrace.toString(), 0);
        tracker.dispatch();
    }

    public void trackError(String category, String action) {
        tracker.trackEvent(category + ":" + applicationVersionName, action, null, 0);
        tracker.dispatch();
    }
}
