package su.geocaching.android.controller;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.controller.apimanager.DownloadGeoCacheTask;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class Controller {
    private static final String TAG = Controller.class.getCanonicalName();

    private static Controller instance;

    private IApiManager apiManager;

    private GeoCacheLocationManager locationManager;
    private CompassManager compassManager;
    private GpsStatusManager gpsStatusManager;
    private ConnectionManager connectionManager;
    private ResourceManager resourceManager;
    private PreferencesManager preferencesManager;
    private DbManager dbManager;
    private CheckpointManager checkpointManager;
    private GeoCache searchingGeoCache;

   

    private Controller() {
        apiManager = new ApiManager();
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
     * @param map
     *            - links to maps, which will be added caches
     * @param upperLeftCorner
     *            - upper left corner of the visible area
     * @param lowerRightCorner
     *            - lower right corner of the visible area
     */
    public void updateSelectedGeoCaches(SelectGeoCacheMap map, GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
        GeoPoint[] d = { upperLeftCorner, lowerRightCorner };
        new DownloadGeoCacheTask(apiManager, map).execute(d);
    }

    /**
     * @return location manager which can send to ILocationAware location updates
     */
    public synchronized GeoCacheLocationManager getLocationManager() {
        if (locationManager == null) {
            LogManager.e(TAG, "location manager wasn't init yet", new NullPointerException("location manager wasn't init yet"));
        }
        return locationManager;
    }

    /**
     * @return compass manager which can send to ICompassAware updates of bearing
     */
    public synchronized CompassManager getCompassManager() {
        if (compassManager == null) {
            LogManager.e(TAG, "compass manager wasn't init yet", new NullPointerException("compass manager wasn't init yet"));
        }
        return compassManager;
    }

    /**
     * @return gps status manager which can send to IGpsStatusAware updates of status gps engine
     */
    public synchronized GpsStatusManager getGpsStatusManager() {
        if (gpsStatusManager == null) {
            LogManager.e(TAG, "gps status manager wasn't init yet", new NullPointerException("gps status manager wasn't init yet"));
        }
        return gpsStatusManager;
    }

    /**
     * @return connection manager which can send to IInternetAware updates of internet connection status
     */
    public synchronized ConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            LogManager.e(TAG, "connection manager wasn't init yet", new NullPointerException("connection manager wasn't init yet"));
        }
        return connectionManager;
    }

    /**
     * @return resource manager which can give you application resources
     */
    public synchronized ResourceManager getResourceManager() {
        if (resourceManager == null) {
            LogManager.e(TAG, "resource manager wasn't init yet", new NullPointerException("resource manager wasn't init yet"));
        }
        return resourceManager;
    }

    /**
     * @return resource manager which can give you application preferences
     */
    public synchronized PreferencesManager getPreferencesManager() {
        if (preferencesManager == null) {
            LogManager.e(TAG, "preferences manager wasn't init yet", new NullPointerException("preferences manager wasn't init yet"));
        }
        return preferencesManager;
    }

    /**
     * @return resource manager which can give you interface to working with database
     */
    public synchronized DbManager getDbManager() {
        if (dbManager == null) {
            LogManager.e(TAG, "db manager wasn't init yet", new NullPointerException("db manager wasn't init yet"));
        }
        return dbManager;
    }

    /**
     * @param context
     *            for init manager
     * @return location manager which can send to ILocationAware location updates
     */
    public synchronized GeoCacheLocationManager getLocationManager(Context context) {
        if (locationManager == null) {
            LogManager.d(TAG, "location manager wasn't init yet. init.");
            locationManager = new GeoCacheLocationManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
        return locationManager;
    }

    /**
     * @param context
     *            for init manager
     * @return compass manager which can send to ICompassAware updates of bearing
     */
    public synchronized CompassManager getCompassManager(Context context) {
        if (compassManager == null) {
            LogManager.d(TAG, "compass manager wasn't init yet. init.");
            compassManager = new CompassManager((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
        }
        return compassManager;
    }

    /**
     * @param context
     *            for init manager
     * @return gps status manager which can send to IGpsStatusAware updates of status gps engine
     */
    public synchronized GpsStatusManager getGpsStatusManager(Context context) {
        if (gpsStatusManager == null) {
            LogManager.d(TAG, "gps status manager wasn't init yet. init.");
            gpsStatusManager = new GpsStatusManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
        return gpsStatusManager;
    }

    /**
     * @param context
     *            for init manager
     * @return connection manager which can send to IInternetAware updates of internet connection status
     */
    public synchronized ConnectionManager getConnectionManager(Context context) {
        if (connectionManager == null) {
            LogManager.d(TAG, "connection manager wasn't init yet. init.");
            connectionManager = new ConnectionManager(context);
        }
        return connectionManager;
    }

    /**
     * @param context
     *            for init manager
     * @return resource manager which can give you application resources
     */
    public synchronized ResourceManager getResourceManager(Context context) {
        if (resourceManager == null) {
            LogManager.d(TAG, "resource manager wasn't init yet. init.");
            resourceManager = new ResourceManager(context);
        }
        return resourceManager;
    }

    /**
     * @param context
     *            for init manager
     * @return resource manager which can give you application preferences
     */
    public synchronized PreferencesManager getPreferencesManager(Context context) {
        if (preferencesManager == null) {
            LogManager.d(TAG, "preferences manager wasn't init yet. init.");
            preferencesManager = new PreferencesManager(context);
        }
        return preferencesManager;
    }

    /**
     * @param context
     *            for init manager
     * @return resource manager which can give you interface to working with database
     */
    public synchronized DbManager getDbManager(Context context) {
        if (dbManager == null) {
            LogManager.d(TAG, "db manager wasn't init yet. init");
            dbManager = new DbManager(context);
        }
        return dbManager;
    }

    /**
     * @return the checkpointManager
     */
    public CheckpointManager getCheckpointManager(int id) {
        if (checkpointManager == null || checkpointManager.getCacheId() != id) {
            LogManager.d(TAG, "checkpoint manager wasn't init yet. init");
            checkpointManager = new CheckpointManager(id);
        }
        return new CheckpointManager(id);
    }

    public GeoCache getSearchingGeoCache() {
        return searchingGeoCache;
    }

    public void setSearchingGeoCache(GeoCache searchedGeoCache) {
        this.searchingGeoCache = searchedGeoCache;
    }

    /**
     * Initialize all managers with fixed context
     * 
     * @param context
     *            which will be used in all managers
     */
    public void initManagers(Context context) {
        if (dbManager == null) {
            dbManager = new DbManager(context);
        }
        if (resourceManager == null) {
            resourceManager = new ResourceManager(context);
        }
        if (preferencesManager == null) {
            preferencesManager = new PreferencesManager(context);
        }
        if (compassManager == null) {
            compassManager = new CompassManager((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
        }
        if (locationManager == null) {
            locationManager = new GeoCacheLocationManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
        if (gpsStatusManager == null) {
            gpsStatusManager = new GpsStatusManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
        if (connectionManager == null) {
            connectionManager = new ConnectionManager(context);
        }
    }
}