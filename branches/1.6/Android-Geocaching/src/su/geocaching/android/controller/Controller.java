package su.geocaching.android.controller;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Vibrator;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import su.geocaching.android.controller.apimanager.GeocachingSuApiManager;
import su.geocaching.android.controller.apimanager.IApiManager;
import su.geocaching.android.controller.managers.*;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.info.InfoViewModel;
import su.geocaching.android.ui.selectmap.SelectMapViewModel;

import java.lang.reflect.Method;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class Controller {
    private static final String TAG = Controller.class.getCanonicalName();
    /**
     * This constant is really needed, because compiler can remove code blocks which cannot be execute.
     * Visibility is public because LogManager and AnalyticsManager use this constant
     */
    public static final boolean DEBUG = false;
    private static final boolean SHOW_MEMORY_TOAST = false;

    private static Controller instance;
    private Context applicationContext;

    private IApiManager apiManager;

    private AccurateUserLocationManager locationManager;
    private LowPowerUserLocationManager lowPowerLocationManager;
    private CompassManager compassManager;
    private ConnectionManager connectionManager;
    private ResourceManager resourceManager;
    private PreferencesManager preferencesManager;
    private DbManager dbManager;
    private CheckpointManager checkpointManager;
    private GoogleAnalyticsManager analyticsManager;
    private CallbackManager callbackManager;
    private ExternalStorageManager externalStorageManager;

    // UI view models
    private SelectMapViewModel selectMapViewModel;
    private InfoViewModel infoViewModel;

    private GeoCache currentSearchPoint;

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
     * @return location manager which can send to ILocationAware location updates
     */
    public synchronized AccurateUserLocationManager getLocationManager() {
        return getLocationManager(applicationContext);
    }

    /**
     * @return location manager which can send to ILocationAware location updates
     */
    public synchronized LowPowerUserLocationManager getLowPowerLocationManager() {
        return getLowPowerLocationManager(applicationContext);
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
     * @return resource manager which can give you interface to working with SD card
     */
    public synchronized ExternalStorageManager getExternalStorageManager() {
        return getExternalStorageManager(applicationContext);
    }

    /**
     * @param context
     *         for init manager
     * @return location manager which can send to ILocationAware location updates
     */
    private synchronized AccurateUserLocationManager getLocationManager(Context context) {
        if (locationManager == null) {
            LogManager.d(TAG, "location manager wasn't init yet. init.");
            locationManager = new AccurateUserLocationManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
        return locationManager;
    }

    /**
     * @param context
     *         for init manager
     * @return location manager which can send to ILocationAware location updates
     */
    private synchronized LowPowerUserLocationManager getLowPowerLocationManager(Context context) {
        if (lowPowerLocationManager == null) {
            lowPowerLocationManager = new LowPowerUserLocationManager((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
        }
        return lowPowerLocationManager;
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
     *         for init manager
     * @return compass manager which can send to IBearingAware updates of bearing
     */
    private synchronized CompassManager getCompassManager(Context context) {
        if (compassManager == null) {
            LogManager.d(TAG, "compass manager wasn't init yet. init.");
            compassManager = new CompassManager((SensorManager) context.getSystemService(Context.SENSOR_SERVICE), getLocationManager());
        }
        return compassManager;
    }

    /**
     * @param context
     *         for init manager
     * @return connection manager which can send to IConnectionAware updates of Internet connection status
     */
    private synchronized ConnectionManager getConnectionManager(Context context) {
        if (connectionManager == null) {
            LogManager.d(TAG, "connection manager wasn't init yet. init.");
            connectionManager = new ConnectionManager(context);
        }
        return connectionManager;
    }

    /**
     * @param context
     *         for init manager
     * @return resource manager which can give you application resources
     */
    private synchronized ResourceManager getResourceManager(Context context) {
        if (resourceManager == null) {
            LogManager.d(TAG, "resource manager wasn't init yet. init.");
            resourceManager = new ResourceManager(context);
        }
        return resourceManager;
    }

    /**
     * @param context
     *         for init manager
     * @return resource manager which can give you application preferences
     */
    private synchronized PreferencesManager getPreferencesManager(Context context) {
        if (preferencesManager == null) {
            LogManager.d(TAG, "preferences manager wasn't init yet. init.");
            preferencesManager = new PreferencesManager(context);
        }
        return preferencesManager;
    }

    /**
     * @param context
     *         for init manager
     * @return resource manager which can give you interface to working with database
     */
    private synchronized DbManager getDbManager(Context context) {
        if (dbManager == null) {
            LogManager.d(TAG, "db manager wasn't init yet. init");
            dbManager = new DbManager(context);
        }
        return dbManager;
    }

    /**
     * @param context
     *         for init manager
     * @return resource manager which can give you interface to working with SD card
     */
    private synchronized ExternalStorageManager getExternalStorageManager(Context context) {
        if (externalStorageManager == null) {
            LogManager.d(TAG, "externalStorageManager manager wasn't init yet. init");
            externalStorageManager = new ExternalStorageManager(context);
        }
        return externalStorageManager;
    }

    public synchronized GoogleAnalyticsManager getGoogleAnalyticsManager() {
        if (analyticsManager == null) {
            LogManager.d(TAG, "GoogleAnalyticsManager wasn't init yet. init");
            analyticsManager = new GoogleAnalyticsManager(applicationContext);
        }
        return analyticsManager;
    }

    /**
     * @param cacheId
     *         cache id
     * @return the checkpointManager
     */
    public CheckpointManager getCheckpointManager(int cacheId) {
        if (checkpointManager == null || checkpointManager.getCacheId() != cacheId) {
            LogManager.d(TAG, "checkpoint manager wasn't init yet. init");
            checkpointManager = new CheckpointManager(cacheId);
        }
        return checkpointManager;
    }

    public GeoCache getCurrentSearchPoint() {
        return currentSearchPoint;
    }

    public void setCurrentSearchPoint(GeoCache currentSearchPoint) {
        this.currentSearchPoint = currentSearchPoint;
    }

    /**
     * Set global application context which will be used for initialize of managers
     *
     * @param applicationContext
     *         global application context of application
     */
    protected void setApplicationContext(Context applicationContext) {
        this.applicationContext = applicationContext;
        if (SHOW_MEMORY_TOAST) {
            MemoryManager memoryManager = new MemoryManager(applicationContext);
            memoryManager.showMemoryToast();
        }
        //TODO: remove after most of the people updated form version 1.3
        getExternalStorageManager(); // initialize and update photo cache
        // increase number of application runs
        getPreferencesManager().setNumberOfRuns(getPreferencesManager().getNumberOfRuns() + 1);
    }

    public String getApplicationVersionName() {
        String versionName = "0.0.0";
        try {
            String packageName = this.applicationContext.getPackageName();
            versionName = this.applicationContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogManager.e(TAG, e.getMessage(), e);
        }
        return versionName;
    }

    public int getScreenRotation() {
        Display display = ((WindowManager) this.applicationContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int rotation;
        try {
            // Since API Level 8
            Method getRotationMethod = Display.class.getMethod("getRotation", new Class[]{});
            rotation = (Integer) getRotationMethod.invoke(display);
        } catch (Exception e) {
            rotation = display.getOrientation();
        }

        if (rotation == Surface.ROTATION_0) return 0;
        if (rotation == Surface.ROTATION_90) return 90;
        if (rotation == Surface.ROTATION_180) return 180;
        if (rotation == Surface.ROTATION_270) return 270;

        return 0;
    }

    public ContentResolver getContentResolver() {
        return applicationContext.getContentResolver();
    }

    public void onTerminate() {
        dbManager.close();
    }

    public synchronized SelectMapViewModel getSelectMapViewModel() {
        if (selectMapViewModel == null) {
            selectMapViewModel = new SelectMapViewModel();
        }
        return selectMapViewModel;
    }

    public synchronized InfoViewModel getInfoViewModel() {
        if (infoViewModel == null) {
            infoViewModel = new InfoViewModel();
        }
        return infoViewModel;
    }

    public void Vibrate() {
        // Get instance of Vibrator from current Context
        Vibrator vibrator = (Vibrator) applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // Vibrate for 25f milliseconds
            vibrator.vibrate(25);
        }
    }
}