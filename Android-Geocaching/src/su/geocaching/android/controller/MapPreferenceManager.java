package su.geocaching.android.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import su.geocaching.android.ui.R;

/**
 * @author: Yuri Denison
 * @since: 24.02.11
 */
public class MapPreferenceManager {
    private static MapPreferenceManager mapPreference;
    private SharedPreferences preferences;
    private Context context;

    private MapPreferenceManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public String getMapTypeString() {
        return preferences.getString(context.getString(R.string.prefer_map_type_key),
            String.valueOf(R.string.prefer_map_type_default_value));
    }

    public Boolean getAddingCacheWayString() {
        return preferences.getBoolean(context.getString(R.string.way_cache_adding_key),
            String.valueOf(R.string.way_cache_adding_default_value).equals("true"));
    }

    public static MapPreferenceManager getPreference(Context context) {
        if (mapPreference == null) {
            mapPreference = new MapPreferenceManager(context);
        }
        return mapPreference;
    }
}
