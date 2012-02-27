package su.geocaching.android.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

public class CompassPreferenceActivity extends PreferenceActivity {
    private static final String COMPASS_PREFERENCE_ACTIVITY_NAME = "/preferences/Compass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(COMPASS_PREFERENCE_ACTIVITY_NAME);

        addPreferencesFromResource(R.xml.compass_preference);
    }

}
