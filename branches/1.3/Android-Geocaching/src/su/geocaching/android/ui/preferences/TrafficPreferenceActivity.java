package su.geocaching.android.ui.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.ui.R;

public class TrafficPreferenceActivity extends PreferenceActivity {
    private static final String MOBILE_TRAFFIC_ACTIVITY_NAME = "/preferences/MobileTraffic";

    /*
    * (non-Javadoc)
    *
    * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(MOBILE_TRAFFIC_ACTIVITY_NAME);

        addPreferencesFromResource(R.xml.traffic_preference);
    }
}