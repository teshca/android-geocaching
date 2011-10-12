package su.geocaching.android.controller.managers;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import android.content.Context;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import java.net.URLEncoder;

public class GoogleAnalyticsManager {
    private static final boolean DEBUG = Controller.DEBUG; // this constant is really need, because compiler can remove code blocks which cannot be execute
    private static final int DISPATCH_INTERVAL = 120; // seconds
    private GoogleAnalyticsTracker tracker;
    private int applicationVersionCode;

    public GoogleAnalyticsManager(Context context) {
        applicationVersionCode = Controller.getInstance().getApplicationVersionCode();
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
            String encodedCategory = category != null ? URLEncoder.encode(category) : "";
            String encodedAction = action != null ? URLEncoder.encode(action) : "";
            String message = ex.getMessage() != null ? ex.getMessage() : "";
            StringBuffer stackTrace = new StringBuffer(message);
            final String NEW_LINE = System.getProperty("line.separator");
            for (StackTraceElement s : ex.getStackTrace()) {
                stackTrace.append(String.format(" at %s.%s(%s:%d)", s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber()));
                stackTrace.append(NEW_LINE);
            }
            tracker.trackEvent(encodedCategory, encodedAction, stackTrace.toString(), applicationVersionCode);
            tracker.dispatch();
        }
    }

    public void trackError(String category, String action) {
        if (!DEBUG) {
            String encodedCategory = category != null ? URLEncoder.encode(category) : "";
            String encodedAction = action != null ? URLEncoder.encode(action) : "";
            tracker.trackEvent(encodedCategory, encodedAction, null, applicationVersionCode);
            tracker.dispatch();
        }
    }
}
