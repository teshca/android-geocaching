package su.geocaching.android.ui.searchmap;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import su.geocaching.android.ui.R;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Apr 1, 2011
 * 
 */
public class SearchMapPreference extends PreferenceActivity {
    /*
     * (non-Javadoc)
     * 
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.search_gc_map_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
