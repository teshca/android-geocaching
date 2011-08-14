package su.geocaching.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.compass.CompassPreferenceActivity;
import su.geocaching.android.ui.geocachemap.MapPreferenceActivity;

public class DashboardPreferenceActivity extends PreferenceActivity {
    private static final String DASHBOARD_PREFERENCE_ACTIVITY_NAME = "/preferences/dashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(DASHBOARD_PREFERENCE_ACTIVITY_NAME);

        addPreferencesFromResource(R.xml.dashboard_preference);

        Preference mapPreference = findPreference("mapPreference");
        mapPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), MapPreferenceActivity.class));
                return true;
            }
        });

        Preference compassPreference = findPreference("compassPreference");
        compassPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), CompassPreferenceActivity.class));
                return true;
            }
        });

        Preference energySavingPreference = findPreference("energySavingPreference");
        energySavingPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), EnergySavingPreferenceActivity.class));
                return true;
            }
        });

        Preference internetPreference = findPreference("internetPreference");
        internetPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                return true;
            }
        });

        Preference gpsPreference = findPreference("gpsPreference");
        gpsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return true;
            }
        });
    }
}