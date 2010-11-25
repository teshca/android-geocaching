package su.geocaching.android.application;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.searchgeocache.GeoCacheCompassManager;
import su.geocaching.android.ui.searchgeocache.GeoCacheLocationManager;
import su.geocaching.android.ui.searchgeocache.GpsStatusListener;
import android.app.Application;
import android.hardware.SensorManager;
import android.location.LocationManager;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 20, 2010
 *      <p>
 *      Class which represent application. It can save common data of different
 *      activities
 *      </p>
 */
public class ApplicationMain extends Application {

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
	
	// debug stub
	desiredGeoCache = new GeoCache(1);
	
	locationManager = new GeoCacheLocationManager((LocationManager) getSystemService(LOCATION_SERVICE));
	compassManager = new GeoCacheCompassManager((SensorManager) getSystemService(SENSOR_SERVICE));
	gpsStatusManager = new GpsStatusListener((LocationManager) getSystemService(LOCATION_SERVICE), getApplicationContext());
    }

    /**
     * @param desired
     *            GeoCache object which user search right now or searched in
     *            past
     */
    public void setDesiredGeoCache(GeoCache desired) {
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
    
    public GpsStatusListener getGpsStatusListener() {
	return gpsStatusManager;
    }
}
