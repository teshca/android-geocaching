package su.geocaching.android.ui;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;

public class AboutActivity extends Activity {
    private static final String TAG = AboutActivity.class.getCanonicalName();
    private static final String ABOUT_ACTIVITY_FOLDER = "/AboutActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "on create");
        setContentView(R.layout.about);

        try {
            this.setTitle(String.format(getString(R.string.application_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (NameNotFoundException e) {
            LogManager.e(TAG, e.getMessage(), e);
        }
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(ABOUT_ACTIVITY_FOLDER);
    }


    public void onExitClick(View v) {
        super.finish();
        LogManager.d(TAG, "exit");
    }
}