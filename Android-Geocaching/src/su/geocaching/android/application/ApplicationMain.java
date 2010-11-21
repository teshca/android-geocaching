package su.geocaching.android.application;

import su.geocaching.android.model.datatype.GeoCache;
import android.app.Application;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 20, 2010
 * @description Class which represent application. It can save common data of
 *              different activities
 */
public class ApplicationMain extends Application {

    private GeoCache desiredGeoCache;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
	super.onCreate();
	desiredGeoCache = new GeoCache(1);
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
}
