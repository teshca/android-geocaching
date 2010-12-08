package su.geocaching.android.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.filter.IFilter;
import su.geocaching.android.controller.filter.NoFilter;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datastorage.GeoCacheStorage;
import su.geocaching.android.model.datastorage.SettingsStorage;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.ConnectionManager;
import su.geocaching.android.ui.searchgeocache.GeoCacheCompassManager;
import su.geocaching.android.ui.searchgeocache.GeoCacheLocationManager;
import su.geocaching.android.ui.searchgeocache.GpsStatusListener;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison
 * @date 04.11.2010
 */
public class Controller {
    private static final String TAG = Controller.class.getCanonicalName();
    private static final String PREFS_NAME = "geocaching_prefs";

    private static Controller instance;

    private IApiManager apiManager;
    private GeoCacheStorage favoriteGeoCacheStorage;
    private SettingsStorage settingsStorage;

    private GeoPoint lastCenter;
    private GeoCache lastSearchedGeoCache;
    private GeoCacheLocationManager locationManager;
    private GeoCacheCompassManager compassManager;
    private GpsStatusListener gpsStatusManager;
    private ConnectionManager connectionManager;
    private static final int DEFAULT_CENTER_LONGITUDE = 29828674;
    private static final int DEFAULT_CENTER_LATITUDE = 59879904;
    private static final int DEFAULT_ZOOM = 13;

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
     * @param maxLatitude
     *            - coordinate of the visible area
     * @param minLatitude
     *            - coordinate of the visible area
     * @param maxLongitude
     *            - coordinate of the visible area
     * @param minLongitude
     */
    public void updateSelectedGeoCaches(SelectGeoCacheMap map, double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) {
	Double[] d = { maxLatitude, minLatitude, maxLongitude, minLongitude };
	new DownloadGeoCacheTask(apiManager, map).execute(d);
    }

    /**
     * Get favorite GeoCaches filtered with chosen filters
     * 
     * @param filterList
     *            - list of filters (if null - no filter)
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
     * @return List of geoCaches filters from settings if
     *         settingsStorage.getFilters() returns null, than return NoFilter
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
     * @param geoCache
     *            we want to draw on the map
     * @param map
     *            -
     * @return Drawable for this geoCache depends on it's parameters
     */
    public Drawable getMarker(GeoCache geoCache, Context map) {
	// TODO: add different icons for different types of geoCache
	switch (geoCache.getStatus()) {
	case VALID:
	    switch (geoCache.getType()) {
	    case TRADITIONAL:
		return getMarker(R.drawable.traditional_cache, map);
	    case VIRTUAL:
		return getMarker(R.drawable.virtual_cache, map);
	    case STEP_BY_STEP:
		return getMarker(R.drawable.step_by_step_cache, map);
	    case EXTREME:
		return getMarker(R.drawable.extreme_cache, map);
	    case EVENT:
		return getMarker(R.drawable.meet_cache, map);
	    }
	    break;
	case NOT_VALID:
	    switch (geoCache.getType()) {
	    case TRADITIONAL:
		return getMarker(R.drawable.traditional_cache, map);
	    case VIRTUAL:
		return getMarker(R.drawable.virtual_cache, map);
	    case STEP_BY_STEP:
		return getMarker(R.drawable.step_by_step_cache, map);
	    case EXTREME:
		return getMarker(R.drawable.extreme_cache, map);
	    case EVENT:
		return getMarker(R.drawable.meet_cache, map);
	    }
	    break;
	case NOT_CONFIRMED:
	    switch (geoCache.getType()) {
	    case TRADITIONAL:
		return getMarker(R.drawable.traditional_cache, map);
	    case VIRTUAL:
		return getMarker(R.drawable.virtual_cache, map);
	    case STEP_BY_STEP:
		return getMarker(R.drawable.step_by_step_cache, map);
	    case EXTREME:
		return getMarker(R.drawable.extreme_cache, map);
	    case EVENT:
		return getMarker(R.drawable.meet_cache, map);
	    }
	    break;
	}
	return null;
    }

    private Drawable getMarker(int resource, Context map) {
	Drawable cacheMarker = map.getResources().getDrawable(resource);
	cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(), cacheMarker.getMinimumWidth(), 0);
	return cacheMarker;
    }

    /**
     * @return location manager which can send to ILocationAware location
     *         updates
     */
    public synchronized GeoCacheLocationManager getLocationManager(Context context) {
	if (locationManager == null) {
	    locationManager = new GeoCacheLocationManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
	    Log.d(TAG, "location manager wasn't init yet. Create it");
	}
	return locationManager;
    }

    /**
     * @return compass manager which can send to ICompassAware updates of
     *         bearing
     */
    public synchronized GeoCacheCompassManager getCompassManager(Context context) {
	if (compassManager == null) {
	    compassManager = new GeoCacheCompassManager((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
	    Log.d(TAG, "compass manager wasn't init yet. Create it");
	}
	return compassManager;
    }

    /**
     * @return gps status manager which can send to IGpsStatusAware updates of
     *         status gps engine
     */
    public synchronized GpsStatusListener getGpsStatusListener(Context context) {
	if (gpsStatusManager == null) {
	    gpsStatusManager = new GpsStatusListener((LocationManager) context.getSystemService(Context.LOCATION_SERVICE), context);
	    Log.d(TAG, "gps status manager wasn't init yet. Create it");
	}
	return gpsStatusManager;
    }

    /**
     * @return connection manager which can send to IInternetAware updates of
     *         internet connection status
     */
    public synchronized ConnectionManager getConnectionManager(Context context) {
	if (connectionManager == null) {
	    connectionManager = new ConnectionManager((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	    Log.d(TAG, "connection manager wasn't init yet. Create it");
	}
	return connectionManager;
    }

    /**
     * Get id of last searched geocache from preferences and get GeoCache object
     * from database
     * 
     * @param context
     *            Context for connection to db and loading preferences
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
     * @param lastSearchedGeoCache
     *            last searched geocache
     * @param context
     *            for connection to db and saving it to preferences
     */
    public synchronized void setLastSearchedGeoCache(GeoCache lastSearchedGeoCache, Context context) {
	if (lastSearchedGeoCache != null) {
	    Log.d(TAG, "Save last searched geocache (id=" + Integer.toString(lastSearchedGeoCache.getId()) + ") in settings");
	    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(GeoCache.class.getCanonicalName(), lastSearchedGeoCache.getId());
	    // Commit the edits!
	    editor.commit();
	}
	this.lastSearchedGeoCache = lastSearchedGeoCache;
    }

    public synchronized void setLastMapInfo(GeoPoint center, int zoom, Context context) {
	if (lastCenter != null) {
	    Log.d(TAG, "Save last map center (" + center.getLatitudeE6() + ", " + center.getLongitudeE6() + ") in settings");
	    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt("center_x", center.getLatitudeE6());
            editor.putInt("center_y", center.getLongitudeE6());
            editor.putInt("zoom", zoom);
	    // Commit the edits!
	    editor.commit();
	}
	this.lastCenter = center;
    }

    /**
     * 
     * @return  [0] - last center latitude
     *          [1] - last center longitude
     *          [2] - last zoom
     */
    public synchronized int[] getLastMapInfo(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        int center_x = settings.getInt("center_x", DEFAULT_CENTER_LATITUDE);
        int center_y = settings.getInt("center_y", DEFAULT_CENTER_LONGITUDE);
        int zoom = settings.getInt("zoom", DEFAULT_ZOOM);
        return new int[]{center_x, center_y, zoom};
    }
}