package su.geocaching.android.ui.compass;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

public class CompassPreferenceActivity extends PreferenceActivity {
    private static final String COMPASS_PREFERENCE_ACTIVITY = "/CompassPreferenceActivity"; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager(this).trackPageView(COMPASS_PREFERENCE_ACTIVITY);
        addPreferencesFromResource(R.xml.compass_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
