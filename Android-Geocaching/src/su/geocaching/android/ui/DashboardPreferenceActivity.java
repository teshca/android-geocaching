package su.geocaching.android.ui;

import su.geocaching.android.controller.LogManager;
import su.geocaching.android.ui.compass.CompassPreferenceActivity;
import su.geocaching.android.ui.selectgeocache.MapPreferenceActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class DashboardPreferenceActivity extends PreferenceActivity {
    private static final String TAG = DashboardPreferenceActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogManager.d(TAG, "onCreate");
        addPreferencesFromResource(R.xml.dashboard_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        Preference mapPreference = (Preference) findPreference("mapPreference");
        mapPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), MapPreferenceActivity.class));
                return true;
            }
        });
        
        Preference compassPreference = (Preference) findPreference("compassPreference");
        compassPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), CompassPreferenceActivity.class));
                return true;
            }
        });
        
        Preference energySavingPreference = (Preference) findPreference("energySavingPreference");
        energySavingPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), EnergySavingPreferenceActivity.class));
                return true;
            }
        });
        
        Preference internetAndGpsPreference = (Preference) findPreference("internetAndGpsPreference");
        internetAndGpsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), InternetAndGpsPreferenceActivity.class));
                return true;
            }
        });
    }
}
