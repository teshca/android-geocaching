package su.geocaching.android.controller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.apimanager.DownloadGeoCachesTask;
import su.geocaching.android.controller.apimanager.GeocachingSuApiManager;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.managers.*;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.selectmap.SelectMapActivity;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class Controller {
    private static final String TAG = Controller.class.getCanonicalName();
    public static final boolean DEBUG = true;// it is constant really need, because compiler can remove code blocks which cannot be execute. Visibility is public because LogManager and
                                             // AnalyticsManager use this constant

    private static Controller instance;
    private Context applicationContext;

    private IApiManager apiManager;

    private UserLocationManager locationManager;
    private CompassManager compassManager;
    private ConnectionManager connectionManager;
    private ResourceManager resourceManager;
    private PreferencesManager preferencesManager;
    private DbManager dbManager;
    private CheckpointManager checkpointManager;
    private GoogleAnalyticsManager analyticsManager;
    private CallbackManager callbackManager;

    private GeoCache searchingGeoCache;

    private Controller() {
        apiManager = new GeocachingSuApiManager();
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
    public void updateSelectedGeoCaches(SelectMapActivity map, GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
        GeoPoint[] d = { upperLeftCorner, lowerRightCorner };
        new DownloadGeoCachesTask(apiManager, map).execute(d);
    }

    /**
     * @return location manager which can send to ILocationAware location updates
     */
    public synchronized UserLocationManager getLocationManager() {
        return getLocationManager(applicationContext);
    }

    /**
     * @return compass manager which can send to IBearingAware updates of bearing
     */
    public synchronized CompassManager getCompassManager() {
        return getCompassManager(applicationContext);
    }

    /**
     * @return ApiManager which can get data from Internet
     */
    public synchronized IApiManager getApiManager() {
        return apiManager;
    }

    /**
     * @return connection manager which can send to IConnectionAware updates of Internet connection status
     */
    public synchronized ConnectionManager getConnectionManager() {
        return getConnectionManager(applicationContext);
    }

    /**
     * @return resource manager which can give you application resources
     */
    public synchronized ResourceManager getResourceManager() {
        return getResourceManager(applicationContext);
    }

    /**
     * @return resource manager which can give you application preferences
     */
    public synchronized PreferencesManager getPreferencesManager() {
        return getPreferencesManager(applicationContext);
    }

    /**
     * @return resource manager which can give you interface to working with database
     */
    public synchronized DbManager getDbManager() {
        return getDbManager(applicationContext);
    }

    /**
     * @param context
     *            for init manager
     * @return location manager which can send to ILocationAware location updates
     */
    public synchronized UserLocationManager getLocationManager(Context context) {
        if (locationManager == null) {
            LogManager.d(TAG, "location manager wasn't init yet. init.");
            locationManager = new UserLocationManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
        return locationManager;
    }

    /**
     * @return callback manager which can send messages to handlers
     */
    public synchronized CallbackManager getCallbackManager() {
        if (callbackManager == null) {
            LogManager.d(TAG, "callback manager wasn't init yet. init.");
            callbackManager = new CallbackManager();
        }
        return callbackManager;
    }

    /**
     * @param context
     *            for init manager
     * @return compass manager which can send to IBearingAware updates of bearing
     */
    public synchronized CompassManager getCompassManager(Context context) {
        if (compassManager == null) {
            LogManager.d(TAG, "compass manager wasn't init yet. init.");
            compassManager = new CompassManager((SensorManager) context.getSystemService(Context.SENSOR_SERVICE), getLocationManager());
        }
        return compassManager;
    }

    /**
     * @param context
     *            for init manager
     * @return connection manager which can send to IConnectionAware updates of Internet connection status
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

    public synchronized GoogleAnalyticsManager getGoogleAnalyticsManager() {
        if (analyticsManager == null) {
            LogManager.d(TAG, "GoogleAnalyticsManager wasn't init yet. init");
            analyticsManager = new GoogleAnalyticsManager(applicationContext);
        }
        return analyticsManager;
    }

    /**
     * @param cacheId cache id
     * @return the checkpointManager
     */
    public CheckpointManager getCheckpointManager(int cacheId) {
        if (checkpointManager == null || checkpointManager.getCacheId() != cacheId) {
            LogManager.d(TAG, "checkpoint manager wasn't init yet. init");
            checkpointManager = new CheckpointManager(cacheId);
        }
        return checkpointManager;
    }

    public GeoCache getSearchingGeoCache() {
        return searchingGeoCache;
    }

    public void setSearchingGeoCache(GeoCache searchedGeoCache) {
        this.searchingGeoCache = searchedGeoCache;
    }

    /**
     * Set global application context which will be used for initialize of managers
     *
     * @param applicationContext
     *            global application context of application
     */
    protected void setApplicationContext(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public String getApplicationVersionName()
    {
        String versionName = "0.0.0";
        try {
            String packageName = this.applicationContext.getPackageName();
            versionName = this.applicationContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogManager.e(TAG, e.getMessage(), e);
        }
        return versionName;
    }

    public void onTerminate() {
        dbManager.close();
    }
}