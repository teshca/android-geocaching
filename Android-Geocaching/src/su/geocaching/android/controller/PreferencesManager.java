package su.geocaching.android.controller;

import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.MapInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Manager which can get access to application preferences
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class PreferencesManager {
    private static final String TAG = PreferencesManager.class.getCanonicalName();

    private Context context;
    private SharedPreferences preferences;
    private DbManager dbManager;

    public PreferencesManager(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        dbManager = new DbManager(context);
    }

    /**
     * Get id of last searched geocache from preferences and get GeoCache object from database
     * 
     * @param context
     *            Context for connection to db and loading preferences
     * @return last searched geocache by user saved in preferences
     */
    public synchronized GeoCache getLastSearchedGeoCache() {
        int desiredGeoCacheId = preferences.getInt(GeoCache.class.getCanonicalName(), -1);
        GeoCache lastSearchedGeoCache = dbManager.getCacheByID(desiredGeoCacheId);
        return lastSearchedGeoCache;
    }

    /**
     * Save last searched geocache id in preferences
     * 
     * @param lastSearchedGeoCache
     *            last searched geoCache
     * @param context
     *            for connection to db and saving it to preferences
     */
    public synchronized void setLastSearchedGeoCache(GeoCache lastSearchedGeoCache) {
        if (lastSearchedGeoCache != null) {
            LogManager.d(TAG, "Save last searched geocache (id=" + Integer.toString(lastSearchedGeoCache.getId()) + ") in settings");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(GeoCache.class.getCanonicalName(), lastSearchedGeoCache.getId());
            editor.commit();
        }
    }

    /**
     * @param info
     *            with data to save
     */
    public synchronized void setLastMapInfo(MapInfo info) {
        if (info != null) {
            LogManager.d(TAG, "Save last map center (" + info.getCenterX() + ", " + info.getCenterY() + ") in settings");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("center_x", info.getCenterX());
            editor.putInt("center_y", info.getCenterY());
            editor.putInt("zoom", info.getZoom());
            editor.commit();
        }
    }

    /**
     * @return MapInfo object with preferences
     */
    public synchronized MapInfo getLastMapInfo() {
        int center_x = preferences.getInt("center_x", MapInfo.DEFAULT_CENTER_LATITUDE);
        int center_y = preferences.getInt("center_y", MapInfo.DEFAULT_CENTER_LONGITUDE);
        int zoom = preferences.getInt("zoom", MapInfo.DEFAULT_ZOOM);
        LogManager.d("lastMapInfo", "X = " + center_x + "; def = " + MapInfo.DEFAULT_CENTER_LATITUDE);
        LogManager.d("lastMapInfo", "Y = " + center_y + "; def = " + MapInfo.DEFAULT_CENTER_LONGITUDE);
        LogManager.d("lastMapInfo", "zoom = " + zoom + "; def = " + MapInfo.DEFAULT_ZOOM);
        return new MapInfo(center_x, center_y, zoom);
    }

    public boolean getKeepScreenOnPreference() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getBoolean(context.getString(R.string.keep_screen_on_key), String.valueOf(R.string.keep_screen_on_default_value).equals("false"));
    }

    public String getMapTypeString() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getString(context.getString(R.string.prefer_map_type_key), String.valueOf(R.string.prefer_map_type_default_value));
    }

    public Boolean getAddingCacheWayString() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getBoolean(context.getString(R.string.way_cache_adding_key), String.valueOf(R.string.way_cache_adding_default_value).equals("true"));
    }

    public Boolean getStatusFilter(GeoCacheStatus status) {
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

    public Boolean getTypeFilter(GeoCacheType type) {
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
