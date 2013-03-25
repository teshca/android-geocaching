package su.geocaching.android.controller.managers;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionParser;
import com.google.analytics.tracking.android.ExceptionReporter;

import java.io.PrintWriter;
import java.io.StringWriter;

public class GoogleAnalyticsManager {

    public GoogleAnalyticsManager(Context context) {
        EasyTracker.getInstance().setContext(context);
        // Change uncaught exception parser...
        // Note: Checking uncaughtExceptionHandler type can be useful if clearing ga_trackingId during development to disable analytics - avoid NullPointerException.
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (uncaughtExceptionHandler instanceof ExceptionReporter) {
            ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
            exceptionReporter.setExceptionParser(new AnalyticsExceptionParser());
        }
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

    private class AnalyticsExceptionParser implements ExceptionParser {

        public String getDescription(String thread, Throwable throwable) {
            return String.format("Thread: %s, Exception: %s", thread, getStackTrace(throwable));
        }

        private String getStackTrace( Throwable ex) {
            final StringWriter stackTrace = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stackTrace);
            ex.printStackTrace(printWriter);
            return stackTrace.toString();
        }
    }
}
