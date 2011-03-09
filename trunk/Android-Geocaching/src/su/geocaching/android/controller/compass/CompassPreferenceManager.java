package su.geocaching.android.controller.compass;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import su.geocaching.android.ui.R;

public class CompassPreferenceManager {

    public static String PREFS_COMPASS_SPEED_KEY, PREFS_COMPASS_APPEARENCE_KEY;
    private static CompassPreferenceManager compassPreference;
    private SharedPreferences preferences;

    private CompassPreferenceManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        PREFS_COMPASS_SPEED_KEY = context.getString(R.string.prefs_speed_key);
        PREFS_COMPASS_APPEARENCE_KEY = context.getString(R.string.prefs_appearance_key);
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public static CompassPreferenceManager getPreference(Context context) {
        if (compassPreference == null) {
            compassPreference = new CompassPreferenceManager(context);
        }
        return compassPreference;
    }
}
