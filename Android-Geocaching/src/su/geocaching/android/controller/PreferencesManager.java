package su.geocaching.android.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.selectgeocache.MapInfo;

import java.util.EnumSet;
import java.util.ListIterator;

/**
 * Manager which can get access to application preferences
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class PreferencesManager {
    private static final String TAG = PreferencesManager.class.getCanonicalName();

    private final Context context;
    private final SharedPreferences preferences;
    private final DbManager dbManager;
    private final Resources resources;

    public PreferencesManager(Context context) {
        this.context = context;
        resources = context.getResources();
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
        editor.putBoolean(context.getString(R.string.save_notebook_always_key), downloadAlways);
        editor.commit();
    }

    public boolean getDownloadNoteBookAlways() {
        return preferences.getBoolean(context.getString(R.string.save_notebook_always_key), false);
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
        return preferences.getBoolean(context.getString(R.string.keep_screen_on_key), context.getResources().getBoolean(R.bool.keep_screen_on_default_value));
    }

    public boolean useSatelliteMap() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return (!preferences.getString(context.getString(R.string.prefer_map_type_key), context.getString(R.string.prefer_map_type_default_value)).equals("MAP"));
    }

    public Boolean getAddingCacheWayString() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getBoolean(context.getString(R.string.way_cache_adding_key), resources.getBoolean(R.bool.use_group_cache_default_value));
    }

    public GpsUpdateFrequency getGpsUpdateFrequency() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        String k = context.getString(R.string.gps_update_frequency_key);
        String dv = context.getString(R.string.gps_update_frequency_default_value);
        String t = preferences.getString(k, dv);
        return GpsUpdateFrequency.valueOf(t);
    }

    public boolean getOdometerOnPreference() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getBoolean(context.getString(R.string.prefer_odometer_key), resources.getBoolean(R.bool.odometer_default_value));
    }

    public String getCompassSpeed() {
        return preferences.getString(context.getString(R.string.prefs_speed_key), context.getString(R.string.prefer_speed_default_value));
    }

    public String getCompassAppearence() {
        return preferences.getString(context.getString(R.string.prefs_appearance_key), context.getString(R.string.prefer_appearance_default_value));
    }

    public String getIconType() {
        return preferences.getString(context.getString(R.string.prefer_icon_key), context.getString(R.string.prefer_icon_default_value));
    }

    public EnumSet<GeoCacheStatus> getStatusFilter() {
        EnumSet<GeoCacheStatus> set = EnumSet.noneOf(GeoCacheStatus.class);
        String rawval = preferences.getString(context.getString(R.string.cache_filter_status), context.getString(R.string.cache_filter_default_value));
        String[] selected = ListMultiSelectPreference.parseStoredValue(rawval);
        if (selected != null) {
            for (String i : selected) {
                try {
                    GeoCacheStatus e = GeoCacheStatus.valueOf(i);
                    switch (e) {
                        case VALID: {
                            set.add(GeoCacheStatus.VALID);
                            break;
                        }
                        case NOT_VALID: {
                            set.add(GeoCacheStatus.NOT_VALID);
                            break;
                        }
                        case NOT_CONFIRMED: {
                            set.add(GeoCacheStatus.NOT_CONFIRMED);
                            break;
                        }
                    }
                } catch (IllegalArgumentException iae) {
                    set.add(GeoCacheStatus.VALID);
                    set.add(GeoCacheStatus.NOT_VALID);
                    set.add(GeoCacheStatus.NOT_CONFIRMED);
                }
            }
        }
        return set;
    }

    public EnumSet<GeoCacheType> getTypeFilter() {
        EnumSet<GeoCacheType> set = EnumSet.noneOf(GeoCacheType.class);
        String rawval = preferences.getString(context.getString(R.string.cache_filter_type), context.getString(R.string.cache_filter_default_value));
        String[] selected = ListMultiSelectPreference.parseStoredValue(rawval);
        if (selected != null) {
            for (String i : selected) {
                try {
                    GeoCacheType e = GeoCacheType.valueOf(i);
                    switch (e) {
                        case TRADITIONAL: {
                            set.add(GeoCacheType.TRADITIONAL);
                            break;
                        }
                        case VIRTUAL: {
                            set.add(GeoCacheType.VIRTUAL);
                            break;
                        }
                        case STEP_BY_STEP: {
                            set.add(GeoCacheType.STEP_BY_STEP);
                            break;
                        }
                        case EXTREME: {
                            set.add(GeoCacheType.EXTREME);
                            break;
                        }
                        case EVENT: {
                            set.add(GeoCacheType.EVENT);
                            break;
                        }
                    }
                } catch (IllegalArgumentException iae) {
                    set.add(GeoCacheType.TRADITIONAL);
                    set.add(GeoCacheType.VIRTUAL);
                    set.add(GeoCacheType.EXTREME);
                    set.add(GeoCacheType.STEP_BY_STEP);
                    set.add(GeoCacheType.EVENT);
                }
            }
        }
        return set;
    }
}
