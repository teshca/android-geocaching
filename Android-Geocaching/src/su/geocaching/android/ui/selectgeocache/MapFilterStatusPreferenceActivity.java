package su.geocaching.android.ui.selectgeocache;

import su.geocaching.android.ui.R;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import su.geocaching.android.controller.LogManager;
import android.preference.PreferenceActivity;

public class MapFilterStatusPreferenceActivity extends PreferenceActivity {
    
    private static final String TAG = MapFilterStatusPreferenceActivity.class.getCanonicalName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        addPreferencesFromResource(R.xml.map_filter_status_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
