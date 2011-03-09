package su.geocaching.android.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
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

    public Boolean getFilterByStatus(GeoCacheStatus status) {
        switch (status) {
            case VALID:
                return preferences.getBoolean(context.getString(R.string.cache_filter_valid), true);
            case NOT_VALID:
                return preferences.getBoolean(context.getString(R.string.cache_filter_not_valid), true);
            case NOT_CONFIRMED:
                return preferences.getBoolean(context.getString(R.string.cache_filter_not_confirmed), true);
            default:
                return true;
        }
    }

    public Boolean getFilterByType(GeoCacheType type) {
        switch (type) {
            case TRADITIONAL:
                return preferences.getBoolean(context.getString(R.string.cache_filter_traditional), true);
            case EXTREME:
                return preferences.getBoolean(context.getString(R.string.cache_filter_extreme), true);
            case STEP_BY_STEP:
                return preferences.getBoolean(context.getString(R.string.cache_filter_stepbystep), true);
            case VIRTUAL:
                return preferences.getBoolean(context.getString(R.string.cache_filter_virtual), true);
            case EVENT:
                return preferences.getBoolean(context.getString(R.string.cache_filter_event), true);
            default:
                return true;
        }
    }
}
