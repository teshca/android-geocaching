package su.geocaching.android.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.ui.compass.CompassPreferenceActivity;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMapPreferenceActivity;

public class DashboardPreferenceActivity extends PreferenceActivity {
    private static final String TAG = DashboardPreferenceActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogManager.d(TAG, "onCreate");
        addPreferencesFromResource(R.xml.dashboard_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Preference mapPreference = findPreference("mapPreference");
        mapPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), SelectGeoCacheMapPreferenceActivity.class));
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

        Preference internetAndGpsPreference = findPreference("internetAndGpsPreference");
        internetAndGpsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), InternetAndGpsPreferenceActivity.class));
                return true;
            }
        });
    }
}
