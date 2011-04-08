package su.geocaching.android.ui;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import su.geocaching.android.controller.LogManager;

public class AboutActivity extends Activity {
    private static final String TAG = AboutActivity.class.getCanonicalName();

    private GoogleAnalyticsTracker tracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");
        setContentView(R.layout.about);

        try {
            this.setTitle(String.format("%s %s", getString(R.string.application_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (NameNotFoundException e) {
            LogManager.e(TAG, e.getMessage(), e);
        }

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.about_activity_folder));
        tracker.dispatch();
    }


    public void onExitClick(View v) {
        tracker.stop();
        super.finish();
        LogManager.d(TAG, "exit");
    }
}