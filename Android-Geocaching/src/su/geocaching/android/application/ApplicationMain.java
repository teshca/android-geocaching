package su.geocaching.android.application;

import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.searchgeocache.GeoCacheCompassManager;
import su.geocaching.android.ui.searchgeocache.GeoCacheLocationManager;
import su.geocaching.android.ui.searchgeocache.GpsStatusListener;
import android.app.Application;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.util.Log;

/**
 * Class which represent application. It can save common data of different
 * activities
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 20, 2010
 */
public class ApplicationMain extends Application {

    private static final String PREFS_NAME = "geocaching_prefs";
    private static final String TAG = ApplicationMain.class.getCanonicalName();

    private GeoCache desiredGeoCache;
    private GeoCacheLocationManager locationManager;
    private GeoCacheCompassManager compassManager;
    private GpsStatusListener gpsStatusManager;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
	super.onCreate();

	// load last searched GeoCache
	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	int desiredGeoCacheId = settings.getInt(GeoCache.class.getCanonicalName(), -1);
	DbManager dbm = new DbManager(getApplicationContext());
	dbm.openDB();
	desiredGeoCache = dbm.getCacheByID(desiredGeoCacheId);
	dbm.closeDB();

	locationManager = new GeoCacheLocationManager((LocationManager) getSystemService(LOCATION_SERVICE));
	compassManager = new GeoCacheCompassManager((SensorManager) getSystemService(SENSOR_SERVICE));
	gpsStatusManager = new GpsStatusListener((LocationManager) getSystemService(LOCATION_SERVICE), getApplicationContext());
	Log.d(TAG, "onCreate");
    }

    /* (non-Javadoc)
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
	super.onTerminate();
	Log.d(TAG, "onTerminate");
	if (desiredGeoCache != null) {
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(GeoCache.class.getCanonicalName(), desiredGeoCache.getId());
	    // Commit the edits!
	    editor.commit();
	}

    }

    /**
     * @param desired
     *            GeoCache object which user search right now or searched in
     *            past
     */
    public void setDesiredGeoCache(GeoCache desired) {
	if (desiredGeoCache != null) {
	    Log.d(TAG, "Save last searched geocache (id=" + Integer.toString(desiredGeoCache.getId()) + ") in settings");
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(GeoCache.class.getCanonicalName(), desiredGeoCache.getId());
	    // Commit the edits!
	    editor.commit();
	}
	desiredGeoCache = desired;
    }

    /**
     * @return GeoCache object which user search right now or searched in past
     */
    public GeoCache getDesiredGeoCache() {
	return desiredGeoCache;
    }

    /**
     * @return location manager which can send to ILocationAware location
     *         updates
     */
    public GeoCacheLocationManager getLocationManager() {
	return locationManager;
    }

    /**
     * @return compass manager which can send to ICompassAware updates of
     *         bearing
     */
    public GeoCacheCompassManager getCompassManager() {
	return compassManager;
    }

    /**
     * @return gps status manager which can send to IGpsStatusAware updates of
     *         status gps engine
     */
    public GpsStatusListener getGpsStatusListener() {
	return gpsStatusManager;
    }
}
