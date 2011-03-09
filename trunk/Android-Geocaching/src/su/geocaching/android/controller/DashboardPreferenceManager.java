package su.geocaching.android.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import su.geocaching.android.ui.R;

public class DashboardPreferenceManager {
    private static DashboardPreferenceManager dasboardPreference;
    private SharedPreferences preferences;
    private Context context;

    private DashboardPreferenceManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public boolean getKeepScreenOnPreference() {
        return preferences.getBoolean(context.getString(R.string.keep_screen_on_key),
                String.valueOf(R.string.keep_screen_on_default_value).equals("false"));

    }

    public static DashboardPreferenceManager getPreference(Context context) {
        if (dasboardPreference == null) {
            dasboardPreference = new DashboardPreferenceManager(context);
        }
        return dasboardPreference;
    }

}
