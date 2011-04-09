package su.geocaching.android.ui.selectgeocache;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import su.geocaching.android.ui.AboutActivity;
import su.geocaching.android.ui.R;

/**
 * @author: Yuri Denison
 * @since: 23.02.11
 */
public class SelectGeoCacheMapPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.select_gc_map_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }
}
