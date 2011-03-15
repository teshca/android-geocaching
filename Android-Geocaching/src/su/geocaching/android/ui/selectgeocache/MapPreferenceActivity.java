package su.geocaching.android.ui.selectgeocache;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import su.geocaching.android.ui.EnergySavingPreferenceActivity;
import su.geocaching.android.ui.R;

/**
 * @author: Yuri Denison
 * @since: 23.02.11
 */
public class MapPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.map_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        Preference mapFilterTypePreference = (Preference) findPreference("mapFilterType");
        mapFilterTypePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), MapFilterTypePreferenceActivity.class));
                return true;
            }
        });
        
        Preference mapFilterStatusPreference = (Preference) findPreference("mapFilterStatus");
        mapFilterStatusPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), MapFilterStatusPreferenceActivity.class));
                return true;
            }
        });
        
    }
}
