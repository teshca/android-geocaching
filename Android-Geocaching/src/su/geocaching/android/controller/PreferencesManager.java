package su.geocaching.android.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.MapInfo;

import java.util.EnumSet;

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
        dbManager = Controller.getInstance().getDbManager();
    }

    /**
     * Get id of last searched geocache from preferences and get GeoCache object from database
     *
     * @return last searched geocache by user saved in preferences
     */
    public synchronized GeoCache getLastSearchedGeoCache() {
        int desiredGeoCacheId = preferences.getInt(GeoCache.class.getCanonicalName(), -1);
        return dbManager.getCacheByID(desiredGeoCacheId);
    }

    /**
     * Save last searched geocache id in preferences
     *
     * @param lastSearchedGeoCache last searched geoCache
     */
    public synchronized void setLastSearchedGeoCache(GeoCache lastSearchedGeoCache) {
        if (lastSearchedGeoCache != null) {
            LogManager.d(TAG, "Save last searched geocache (id=" + Integer.toString(lastSearchedGeoCache.getId()) + ") in settings");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(GeoCache.class.getCanonicalName(), lastSearchedGeoCache.getId());
            editor.commit();
        }
    }

    //}

    /**
     * @param info with data to save
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

    public void setDownloadNoteBookAlways(boolean downloadAlways) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.save_notebook_alwayse_key), downloadAlways);
        editor.commit();
    }

    public boolean getDownloadNoteBookAlways() {
        return preferences.getBoolean(context.getString(R.string.save_notebook_alwayse_key), false);
    }

    /**
     * @return MapInfo object with preferences
     */
    public synchronized MapInfo getLastMapInfo() {
        int center_x = preferences.getInt("center_x", MapInfo.DEFAULT_CENTER_LATITUDE);
        int center_y = preferences.getInt("center_y", MapInfo.DEFAULT_CENTER_LONGITUDE);
        int zoom = preferences.getInt("zoom", MapInfo.DEFAULT_ZOOM);
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

    public EnumSet<GeoCacheStatus> getStatusFilter() {
        EnumSet<GeoCacheStatus> set = EnumSet.noneOf(GeoCacheStatus.class);
        if (preferences.getBoolean(context.getString(R.string.cache_filter_valid), true)) {
            set.add(GeoCacheStatus.VALID);
        }
        if (preferences.getBoolean(context.getString(R.string.cache_filter_not_valid), true)) {
            set.add(GeoCacheStatus.NOT_VALID);
        }
        if (preferences.getBoolean(context.getString(R.string.cache_filter_not_confirmed), true)) {
            set.add(GeoCacheStatus.NOT_CONFIRMED);
        }
        return set;
    }

    public EnumSet<GeoCacheType> getTypeFilter() {
        EnumSet<GeoCacheType> set = EnumSet.noneOf(GeoCacheType.class);
        if (preferences.getBoolean(context.getString(R.string.cache_filter_traditional), true)) {
            set.add(GeoCacheType.TRADITIONAL);
        }
        if (preferences.getBoolean(context.getString(R.string.cache_filter_extreme), true)) {
            set.add(GeoCacheType.EXTREME);
        }
        if (preferences.getBoolean(context.getString(R.string.cache_filter_stepbystep), true)) {
            set.add(GeoCacheType.STEP_BY_STEP);
        }
        if (preferences.getBoolean(context.getString(R.string.cache_filter_virtual), true)) {
            set.add(GeoCacheType.VIRTUAL);
        }
        if (preferences.getBoolean(context.getString(R.string.cache_filter_event), true)) {
            set.add(GeoCacheType.VIRTUAL);
        }
        return set;
    }
}
