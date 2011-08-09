package su.geocaching.android.ui;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;

public class AboutActivity extends Activity {
    private static final String TAG = AboutActivity.class.getCanonicalName();
    private static final String ABOUT_ACTIVITY_NAME = "/AboutActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            this.setTitle(getString(R.string.about_application_version, versionName));
        } catch (NameNotFoundException e) {
            LogManager.e(TAG, e.getMessage(), e);
        }

        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(ABOUT_ACTIVITY_NAME);
    }

    public void onExitClick(View v) {
        this.finish();
    }
}