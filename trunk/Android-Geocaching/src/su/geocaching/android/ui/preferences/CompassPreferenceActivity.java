package su.geocaching.android.ui.preferences;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.ui.R;

public class CompassPreferenceActivity extends SherlockPreferenceActivity {
    private static final String COMPASS_PREFERENCE_ACTIVITY_NAME = "/preferences/Compass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(COMPASS_PREFERENCE_ACTIVITY_NAME);
        getSupportActionBar().setHomeButtonEnabled(true);
        addPreferencesFromResource(R.xml.compass_preference);

        final ListPreference compassSpeed = (ListPreference) findPreference(getString(R.string.prefs_speed_key));
        compassSpeed.setOnPreferenceChangeListener(updateStatusOnListPreferenceChangeListener);
        compassSpeed.setSummary(compassSpeed.getEntry());

        final ListPreference compassAppearance = (ListPreference) findPreference(getString(R.string.prefs_appearance_key));
        compassAppearance.setOnPreferenceChangeListener(updateStatusOnListPreferenceChangeListener);
        compassAppearance.setSummary(compassAppearance.getEntry());

        final ListPreference compassSensor = (ListPreference) findPreference(getString(R.string.prefs_sensor_key));
        compassSensor.setOnPreferenceChangeListener(updateStatusOnListPreferenceChangeListener);
        compassSensor.setSummary(compassSensor.getEntry());
    }

    private Preference.OnPreferenceChangeListener updateStatusOnListPreferenceChangeListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final ListPreference listPreference = (ListPreference) preference;
                    if (listPreference == null) return false;
                    preference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue((String) newValue)]);
                    return true;
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavigationManager.startDashboardActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
