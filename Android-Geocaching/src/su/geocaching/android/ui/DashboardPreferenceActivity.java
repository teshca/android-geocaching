package su.geocaching.android.ui;

import su.geocaching.android.controller.LogManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DashboardPreferenceActivity extends PreferenceActivity {
    private static final String TAG = DashboardPreferenceActivity.class.getCanonicalName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogManager.d(TAG, "CCCCCCC");
        addPreferencesFromResource(R.xml.dashboard_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
