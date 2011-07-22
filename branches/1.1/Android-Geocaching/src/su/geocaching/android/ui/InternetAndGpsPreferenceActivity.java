package su.geocaching.android.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;

public class InternetAndGpsPreferenceActivity extends PreferenceActivity {

    private static final String TAG = InternetAndGpsPreferenceActivity.class.getCanonicalName();
    private static final String PREFERENCE_ACTIVITY_FOLDER = "/InternetAndGpsPreferenceActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(PREFERENCE_ACTIVITY_FOLDER);
        
        LogManager.d(TAG, "onCreate");
        addPreferencesFromResource(R.xml.internet_and_gps_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        
        Preference internetPreference = findPreference("internetPreference");
        internetPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
                return true;
            }
        });

        Preference gpsPreference = findPreference("gpsPreference");
        gpsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                return true;
            }
        });
    }
}
