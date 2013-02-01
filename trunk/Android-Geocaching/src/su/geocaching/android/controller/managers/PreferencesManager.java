package su.geocaching.android.controller.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.controller.ListMultiSelectPreference;
import su.geocaching.android.model.*;
import su.geocaching.android.ui.R;

import java.util.EnumSet;

/**
 * Manager which can get access to application preferences
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since March 2011
 */
public class PreferencesManager {
    private static final String TAG = PreferencesManager.class.getCanonicalName();

    private static final String NUMBER_OF_RUNS_PREFERENCE_KEY = "number_of_runs_preffference_key";
    private static final String ASK_FOR_RATING_SHOWN_PREFERENCE_KEY = "ask_for_rating_preffference_key";

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
     * @param lastSearchedGeoCache
     *         last searched geoCache
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
     *         with data to save
     */
    public synchronized void setLastSelectMapInfo(MapInfo info) {
        if (info != null) {
            LogManager.d(TAG, "Save last map center (" + info.getCenterX() + ", " + info.getCenterY() + ") in settings");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("selectmap_center_x", info.getCenterX());
            editor.putInt("selectmap_center_y", info.getCenterY());
            editor.putInt("selectmap_zoom", info.getZoom());
            editor.commit();
        }
    }

    /**
     * @return MapInfo object with preferences
     */
    public synchronized MapInfo getLastSelectMapInfo() {
        LocationManager locationManager = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        int default_x, default_y;
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation == null) {
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (networkLocation == null) {
                default_x = MapInfo.DEFAULT_CENTER_LATITUDE;
                default_y = MapInfo.DEFAULT_CENTER_LONGITUDE;
            } else {
                default_x = (int) (networkLocation.getLatitude() * 1E6);
                default_y = (int) (networkLocation.getLongitude() * 1E6);
            }
        } else {
            default_x = (int) (gpsLocation.getLatitude() * 1E6);
            default_y = (int) (gpsLocation.getLongitude() * 1E6);
        }
        int center_x = preferences.getInt("selectmap_center_x", default_x);
        int center_y = preferences.getInt("selectmap_center_y", default_y);
        int zoom = preferences.getInt("selectmap_zoom", MapInfo.DEFAULT_ZOOM);
        return new MapInfo(center_x, center_y, zoom);
    }

    /**
     * @param info
     *         with data to save
     */
    public synchronized void setLastSearchMapInfo(SearchMapInfo info) {
        if (info != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("searchmap_center_x", info.getCenterX());
            editor.putInt("searchmap_center_y", info.getCenterY());
            editor.putInt("searchmap_zoom", info.getZoom());
            editor.putInt("searchmap_cacheid", info.getGeoCacheId());
            editor.commit();
        }
    }

    /**
     * @return MapInfo object with preferences
     */
    public synchronized SearchMapInfo getLastSearchMapInfo() {
        int center_x = preferences.getInt("searchmap_center_x", MapInfo.DEFAULT_CENTER_LATITUDE);
        int center_y = preferences.getInt("searchmap_center_y", MapInfo.DEFAULT_CENTER_LONGITUDE);
        int zoom = preferences.getInt("searchmap_zoom", MapInfo.DEFAULT_ZOOM);
        int cacheId = preferences.getInt("searchmap_cacheid", -1);
        return new SearchMapInfo(center_x, center_y, zoom, cacheId);
    }

    public void setRemoveFavoriteWithoutConfirm(boolean forceRemove) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.remove_cache_without_confirm_key), forceRemove);
        editor.commit();
    }

    public boolean getRemoveFavoriteWithoutConfirm() {
        return preferences.getBoolean(context.getString(R.string.remove_cache_without_confirm_key), false);
    }

    public void setDownloadPhotosAlways(boolean downloadPhotosAlways) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.download_photos_always_key), downloadPhotosAlways);
        editor.commit();
    }

    public boolean getDownloadPhotosAlways() {
        return preferences.getBoolean(context.getString(R.string.download_photos_always_key), false);
    }

    public void setNumberOfRuns(int numberOfRuns) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(NUMBER_OF_RUNS_PREFERENCE_KEY, numberOfRuns);
        editor.commit();
    }

    public int getNumberOfRuns() {
        return preferences.getInt(NUMBER_OF_RUNS_PREFERENCE_KEY, 0);
    }

    public boolean isAskForRatingShown() {
        return preferences.getBoolean(ASK_FOR_RATING_SHOWN_PREFERENCE_KEY, false);
    }

    public void setAskForRatingShown() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ASK_FOR_RATING_SHOWN_PREFERENCE_KEY, true);
        editor.commit();
    }

    /*
    public void setDownloadNoteBookAlways(boolean downloadNotebookAlways) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.save_notebook_always_key), downloadNotebookAlways);
        editor.commit();
    }

    public boolean getDownloadNoteBookAlways() {
        return preferences.getBoolean(context.getString(R.string.save_notebook_always_key), false);
    }
    */

    public boolean getKeepScreenOnPreference() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getBoolean(context.getString(R.string.keep_screen_on_key), context.getResources().getBoolean(R.bool.keep_screen_on_default_value));
    }

    public boolean useSatelliteMap() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        String mapValue = context.getString(R.string.prefer_map_type_default_value);
        return (!preferences.getString(context.getString(R.string.prefer_map_type_key), mapValue).equals(mapValue));
    }

    public Boolean isCacheGroupingEnabled() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getBoolean(context.getString(R.string.use_group_cache_key), resources.getBoolean(R.bool.use_group_cache_default_value));
    }

    public GpsUpdateFrequency getGpsUpdateFrequency() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        String k = context.getString(R.string.gps_update_frequency_key);
        String dv = context.getString(R.string.gps_update_frequency_default_value);
        String t = preferences.getString(k, dv);
        return GpsUpdateFrequency.valueOf(t);
    }

    public boolean isOdometerOnPreference() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getBoolean(context.getString(R.string.prefer_odometer_key), resources.getBoolean(R.bool.prefer_odometer_default_value));
    }

    public void setOdometerOnPreference(boolean odometerFlag) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.prefer_odometer_key), odometerFlag);
        editor.commit();
    }

    public String getCompassSpeed() {
        return preferences.getString(context.getString(R.string.prefs_speed_key), context.getString(R.string.prefer_speed_default_value));
    }

    public String getCompassAppearance() {
        return preferences.getString(context.getString(R.string.prefs_appearance_key), context.getString(R.string.prefer_appearance_default_value));
    }

    public boolean isUsingGpsCompassPreference() {
        // keys located in resources, because settings logic described in xml and write it automatically to SharedPreferences
        return preferences.getString(context.getString(R.string.prefs_sensor_key), context.getString(R.string.sensor_preference_default_value)).endsWith("GPS");
    }

    public int getFavoritesSortType() {
        return preferences.getInt(context.getString(R.string.prefs_favorites_sort_key), 0);
    }

    public void setFavoritesSortType(int sortType) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.prefs_favorites_sort_key), sortType);
        editor.commit();
    }


    public String getIconType() {
        return preferences.getString(context.getString(R.string.prefer_icon_key), context.getString(R.string.prefer_icon_default_value));
    }

    public EnumSet<GeoCacheStatus> getStatusFilter() {
        EnumSet<GeoCacheStatus> set = EnumSet.noneOf(GeoCacheStatus.class);
        String cache_filter_default_value = context.getString(R.string.cache_filter_default_value);
        String rawval = preferences.getString(context.getString(R.string.cache_filter_status), cache_filter_default_value);
        if (rawval.equals(cache_filter_default_value)) {
            for (GeoCacheStatus i : GeoCacheStatus.values()) {
                set.add(i);
            }
        } else {
            String[] selected = ListMultiSelectPreference.parseStoredValue(rawval);
            if (selected != null) {
                for (String cacheStatus : selected) {
                    try {
                        set.add(GeoCacheStatus.valueOf(cacheStatus));
                    } catch (IllegalArgumentException e) {
                        LogManager.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
        return set;
    }

    public EnumSet<GeoCacheType> getTypeFilter() {
        EnumSet<GeoCacheType> set = EnumSet.noneOf(GeoCacheType.class);
        String cache_filter_default_value = context.getString(R.string.cache_filter_default_value);
        String rawval = preferences.getString(context.getString(R.string.cache_filter_type), cache_filter_default_value);
        if (rawval.equals(cache_filter_default_value)) {
            for (GeoCacheType i : GeoCacheType.values()) {
                set.add(i);
            }
        } else {
            String[] selected = ListMultiSelectPreference.parseStoredValue(rawval);
            if (selected != null) {
                for (String cacheType : selected) {
                    try {
                        set.add(GeoCacheType.valueOf(cacheType));
                    } catch (IllegalArgumentException e) {
                        LogManager.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
        return set;
    }
}
