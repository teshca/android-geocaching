package su.geocaching.android.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.controller.apimanager.DownloadGeoCacheTask;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.filter.IFilter;
import su.geocaching.android.controller.filter.NoFilter;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datastorage.GeoCacheStorage;
import su.geocaching.android.model.datastorage.SettingsStorage;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.ConnectionManager;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.utils.log.LogHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison
 * @date 04.11.2010
 */
public class Controller {
    private static final String TAG = Controller.class.getCanonicalName();
    private static final String PREFS_NAME = "geocaching_prefs";
    
    private static final int DEFAULT_CENTER_LONGITUDE = 29828674;
    private static final int DEFAULT_CENTER_LATITUDE = 59879904;
    private static final int DEFAULT_ZOOM = 13;
    
    private static Controller instance;

    private IApiManager apiManager;
    private GeoCacheStorage favoriteGeoCacheStorage;
    private SettingsStorage settingsStorage;

    //private GeoPoint lastCenter;
    private GeoCache lastSearchedGeoCache;
    private GeoCacheLocationManager locationManager;
    private CompassManager compassManager;
    private GpsStatusManager gpsStatusManager;
    private ConnectionManager connectionManager;
    private ResourceManager resourceManager;
    private GeoCache searchingGeoCache;

	private Controller() {
        apiManager = new ApiManager();
        favoriteGeoCacheStorage = GeoCacheStorage.getInstance();
        settingsStorage = SettingsStorage.getInstance();
    }

    public static Controller getInstance() {
        if (instance == null) {
            synchronized (Controller.class) {
                if (instance == null) {
                    instance = new Controller();
                }
            }
        }
        return instance;
    }

    /**
     * Request for caches in the visible region
     *
     * @param map              - links to maps, which will be added caches
     * @param upperLeftCorner  - upper left corner of the visible area
     * @param lowerRightCorner - lower right corner of the visible area
     */
    public void updateSelectedGeoCaches(SelectGeoCacheMap map, GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
        GeoPoint[] d = {upperLeftCorner, lowerRightCorner};
        new DownloadGeoCacheTask(apiManager, map).execute(d);
    }

    /**
     * Get favorite GeoCaches filtered with chosen filters
     *
     * @param filterList - list of filters (if null - no filter)
     * @return LinkedList<GeoCache>
     */
    public List<GeoCache> getFavoriteGeoCaches(List<IFilter> filterList) {
        if (filterList == null) {
            return favoriteGeoCacheStorage.getGeoCacheList();
        } else {
            List<GeoCache> list = favoriteGeoCacheStorage.getGeoCacheList();
            for (IFilter filter : filterList) {
                list = filter.filter(list);
            }
            return list;
        }
    }

    /**
     * @return List of geoCaches filters from settings if settingsStorage.getFilters() returns null, than return NoFilter
     */
    public LinkedList<IFilter> getFilterList() {
        LinkedList<IFilter> list = new LinkedList<IFilter>();
        if (settingsStorage.getFilters() == null) {
            list.add(NoFilter.getInstance());
        } else {
            list.addAll(settingsStorage.getFilters());
        }
        return list;
    }

    /**
     * @param geoCache we want to draw on the map
     * @return Drawable for this geoCache depends on it's parameters
     */
    public Drawable getMarker(GeoCache geoCache) {
        switch (geoCache.getType()) {
            case TRADITIONAL:
                switch (geoCache.getStatus()) {
                    case VALID:
                        return getMarker(R.drawable.ic_cache_traditional_valid);
                    case NOT_VALID:
                        return getMarker(R.drawable.ic_cache_traditional_not_valid);
                    case NOT_CONFIRMED:
                        return getMarker(R.drawable.ic_cache_traditional_not_confirmed);
                }
                break;
            case VIRTUAL:
                switch (geoCache.getStatus()) {
                    case VALID:
                        return getMarker(R.drawable.ic_cache_virtual_valid);
                    case NOT_VALID:
                        return getMarker(R.drawable.ic_cache_virtual_not_valid);
                    case NOT_CONFIRMED:
                        return getMarker(R.drawable.ic_cache_virtual_not_confirmed);
                }
                break;
            case STEP_BY_STEP:
                switch (geoCache.getStatus()) {
                    case VALID:
                        return getMarker(R.drawable.ic_cache_stepbystep_valid);
                    case NOT_VALID:
                        return getMarker(R.drawable.ic_cache_stepbystep_not_valid);
                    case NOT_CONFIRMED:
                        return getMarker(R.drawable.ic_cache_stepbystep_not_confirmed);
                }
                break;
            case EXTREME:
                switch (geoCache.getStatus()) {
                    case VALID:
                        return getMarker(R.drawable.ic_cache_extreme_valid);
                    case NOT_VALID:
                        return getMarker(R.drawable.ic_cache_extreme_not_valid);
                    case NOT_CONFIRMED:
                        return getMarker(R.drawable.ic_cache_extreme_not_confirmed);
                }
                break;
            case EVENT:
                switch (geoCache.getStatus()) {
                    case VALID:
                        return getMarker(R.drawable.ic_cache_event_valid);
                    case NOT_VALID:
                        return getMarker(R.drawable.ic_cache_event_not_valid);
                    case NOT_CONFIRMED:
                        return getMarker(R.drawable.ic_cache_event_not_confirmed);
                }
                break;
            case GROUP:
                return getMarker(R.drawable.ic_cache_group);
            case CHECKPOINT:
                return getMarker(R.drawable.cache);
        }
        return null;
    }

    private Drawable getMarker(int resource) {
        Drawable cacheMarker = resourceManager.getDrawable(resource);
        cacheMarker.setBounds(-cacheMarker.getMinimumWidth() / 2, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth() / 2, 0);
        return cacheMarker;
    }

    /**
     * @return location manager which can send to ILocationAware location updates
     */
    public synchronized GeoCacheLocationManager getLocationManager() {
        if (locationManager == null) {
            LogHelper.e(TAG, "location manager wasn't init yet", new NullPointerException("location manager wasn't init yet"));
        }
        return locationManager;
    }

    /**
     * @return compass manager which can send to ICompassAware updates of bearing
     */
    public synchronized CompassManager getCompassManager() {
        if (compassManager == null) {
            LogHelper.e(TAG, "compass manager wasn't init yet", new NullPointerException("compass manager wasn't init yet"));
        }
        return compassManager;
    }

    /**
     * @return gps status manager which can send to IGpsStatusAware updates of status gps engine
     */
    public synchronized GpsStatusManager getGpsStatusManager() {
        if (gpsStatusManager == null) {
            LogHelper.e(TAG, "gps status manager wasn't init yet", new NullPointerException("gps status manager wasn't init yet"));
        }
        return gpsStatusManager;
    }

    /**
     * @return connection manager which can send to IInternetAware updates of internet connection status
     */
    public synchronized ConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            LogHelper.e(TAG, "connection manager wasn't init yet", new NullPointerException("connection manager wasn't init yet"));
        }
        return connectionManager;
    }
    
    /**
     * @return resource manager which can give you application resources
     */
    public synchronized ResourceManager getResourceManager() {
        if (resourceManager == null) {
            LogHelper.e(TAG, "resource manager wasn't init yet", new NullPointerException("resource manager wasn't init yet"));
        }
        return resourceManager;
    }

    /**
     * Get id of last searched geocache from preferences and get GeoCache object from database
     *
     * @param context Context for connection to db and loading preferences
     * @return last searched geocache by user saved in preferences
     */
    public synchronized GeoCache getLastSearchedGeoCache(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        int desiredGeoCacheId = settings.getInt(GeoCache.class.getCanonicalName(), -1);
        DbManager dbm = new DbManager(context);
        dbm.openDB();
        lastSearchedGeoCache = dbm.getCacheByID(desiredGeoCacheId);
        dbm.closeDB();
        return lastSearchedGeoCache;
    }

    /**
     * Save last searched geocache id in preferences
     *
     * @param lastSearchedGeoCache last searched geocache
     * @param context              for connection to db and saving it to preferences
     */
    public synchronized void setLastSearchedGeoCache(GeoCache lastSearchedGeoCache, Context context) {
        if (lastSearchedGeoCache != null) {
            LogHelper.d(TAG, "Save last searched geocache (id=" + Integer.toString(lastSearchedGeoCache.getId()) + ") in settings");
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(GeoCache.class.getCanonicalName(), lastSearchedGeoCache.getId());
            // Commit the edits!
            editor.commit();
        }
        this.lastSearchedGeoCache = lastSearchedGeoCache;
    }

    public synchronized void setLastMapInfo(GeoPoint center, int zoom, Context context) {
        if (center != null) {
            LogHelper.d(TAG, "Save last map center (" + center.getLatitudeE6() + ", " + center.getLongitudeE6() + ") in settings");
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("center_x", center.getLatitudeE6());
            editor.putInt("center_y", center.getLongitudeE6());
            editor.putInt("zoom", zoom);
            // Commit the edits!
            editor.commit();
        }
        //this.lastCenter = center;
    }

    /**
     * @return [0] - last center latitude [1] - last center longitude [2] - last zoom
     */
    public synchronized int[] getLastMapInfo(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        int center_x = settings.getInt("center_x", DEFAULT_CENTER_LATITUDE);
        int center_y = settings.getInt("center_y", DEFAULT_CENTER_LONGITUDE);
        int zoom = settings.getInt("zoom", DEFAULT_ZOOM);
        LogHelper.d("lastMapInfo", "X = " + center_x + "; def = " + DEFAULT_CENTER_LATITUDE);
        LogHelper.d("lastMapInfo", "Y = " + center_y + "; def = " + DEFAULT_CENTER_LONGITUDE);
        LogHelper.d("lastMapInfo", "zoom = " + zoom + "; def = " + DEFAULT_ZOOM);
        return new int[]{center_x, center_y, zoom};
    }
    
    public GeoCache getSearchingGeoCache() {
		return searchingGeoCache;
	}

	public void setSearchingGeoCache(GeoCache searchedGeoCache) {
		this.searchingGeoCache = searchedGeoCache;
	}

    public boolean getWayCacheAdding(Context context) {
        return MapPreferenceManager.getPreference(context).getAddingCacheWayString();
    }

    public String getMapTypeString(Context context) {
        return MapPreferenceManager.getPreference(context).getMapTypeString();
    }
    
    public boolean getKeepScreenOnPreference(Context context){
    	return DashboardPreferenceManager.getPreference(context).getKeepScreenOnPreference();
    }
    
    public void initManagers(Context context) {
        compassManager = new CompassManager((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
        locationManager = new GeoCacheLocationManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        gpsStatusManager = new GpsStatusManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        connectionManager = new ConnectionManager((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        resourceManager = new ResourceManager(context);
    }
}