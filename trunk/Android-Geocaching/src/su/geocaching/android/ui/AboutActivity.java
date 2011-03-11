package su.geocaching.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;

public class AboutActivity extends Activity {
    private static final String TAG = AboutActivity.class.getCanonicalName();

    private GoogleAnalyticsTracker tracker;
    private TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");
        setContentView(R.layout.about);
        tv = (TextView) findViewById(R.id.about_content);
        tracker = GoogleAnalyticsTracker.getInstance();

        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.about_activity_folder));
        tracker.dispatch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv.setKeepScreenOn(Controller.getInstance().getKeepScreenOnPreference(tv.getContext()));
    }

    public void onExitClick(View v) {
        tracker.stop();
        super.finish();
        LogManager.d(TAG, "exit");
    }
}