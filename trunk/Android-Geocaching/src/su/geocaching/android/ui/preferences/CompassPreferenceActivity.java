package su.geocaching.android.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.CheckBox;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.UserLocationManager;
import su.geocaching.android.ui.R;

public class CompassPreferenceActivity extends PreferenceActivity {
    private static final String COMPASS_PREFERENCE_ACTIVITY_NAME = "/preference/Compass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(COMPASS_PREFERENCE_ACTIVITY_NAME);

        addPreferencesFromResource(R.xml.compass_preference);
    }

    public void onOdometerClick(View v) {
        UserLocationManager lm = Controller.getInstance().getLocationManager();
        lm.refreshOdometer();
        if (((CheckBox) v).isChecked()) {
            lm.setUpdatingOdometer(true);
        } else {
            lm.setUpdatingOdometer(false);
        }
    }
}
