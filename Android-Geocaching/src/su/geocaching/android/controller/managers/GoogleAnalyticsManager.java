package su.geocaching.android.controller.managers;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import android.content.Context;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import java.net.URLEncoder;

public class GoogleAnalyticsManager {
    private static final boolean DEBUG = Controller.DEBUG; // it is constant really need, because compiler can remove code blocks which cannot be execute
    private static final int DISPATCH_INTERVAL = 120; // Seconds
    private GoogleAnalyticsTracker tracker;

    public GoogleAnalyticsManager(Context context) {
        if (!DEBUG) {
            tracker = GoogleAnalyticsTracker.getInstance();
            tracker.start(context.getString(R.string.id_Google_Analytics), DISPATCH_INTERVAL, context);
        }
    }

    public void trackActivityLaunch(String activityName) {
        if (!DEBUG) {
            activityName = URLEncoder.encode(activityName);
            tracker.trackPageView(activityName);
        }
    }

    public void trackExternalActivityLaunch(String activityName) {
        if (!DEBUG) {
            activityName = URLEncoder.encode(activityName);
            tracker.trackPageView("/external" + activityName);
        }
    }

    public void trackException(String category, String action, Throwable ex) {
        if (!DEBUG) {
            category = URLEncoder.encode(category);
            action = URLEncoder.encode(action);
            StringBuffer stackTrace = new StringBuffer(ex.getMessage());
            final String NEW_LINE = System.getProperty("line.separator");
            for (StackTraceElement s : ex.getStackTrace()) {
                stackTrace.append(String.format(" at %s.%s(%s:%d)", s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber()));
                stackTrace.append(NEW_LINE);
            }
            tracker.trackEvent(category, action, stackTrace.toString(), 0);
            tracker.dispatch();
        }
    }

    public void trackError(String category, String action) {
        if (!DEBUG) {
            category = URLEncoder.encode(category);
            action = URLEncoder.encode(action);
            tracker.trackEvent(category, action, null, 0);
            tracker.dispatch();
        }
    }
}
