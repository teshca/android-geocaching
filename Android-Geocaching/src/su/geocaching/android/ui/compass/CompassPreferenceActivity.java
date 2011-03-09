package su.geocaching.android.ui.compass;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import su.geocaching.android.ui.R;

public class CompassPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.compass_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
