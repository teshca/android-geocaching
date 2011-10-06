package su.geocaching.android.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * @author: Yuri Denison
 * @since: 23.02.11
 */
public class MapPreferenceActivity extends PreferenceActivity {

    private static final String MAP_PREFERENCE_ACTIVITY_NAME= "/preferences/Map";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(MAP_PREFERENCE_ACTIVITY_NAME);
        addPreferencesFromResource(R.xml.map_preference);
    }
}
