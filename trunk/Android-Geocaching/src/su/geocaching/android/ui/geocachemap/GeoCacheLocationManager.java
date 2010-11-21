package su.geocaching.android.ui.geocachemap;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 * @description Location manager which get updates of location by GPS or
 *              GSM/Wi-Fi
 */
public class GeoCacheLocationManager implements LocationListener {

    private ILocationAware context;
    private LocationManager locationManager;
    private Location lastLocation;
    private String provider;
    private boolean locationFixed;

    /**
     * @param context
     *            Activity which use this sensor
     * @param locationManager
     *            location manager of context
     */
    public GeoCacheLocationManager(ILocationAware context, LocationManager locationManager) {
	this.context = context;
	this.locationManager = locationManager;
	locationFixed = false;

    }

    /**
     * Update location obtained from LocationManager
     */
    @Override
    public void onLocationChanged(Location location) {
	lastLocation = location;
	locationFixed = true;
	if ((context instanceof ICompassAware) && (!((ICompassAware) context).isCompassAvailable())) {
	    ((ICompassAware) context).updateAzimuth((int) location.getBearing());
	}
	updateLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
	// TODO: implement onProviderDisabled
    }

    @Override
    public void onProviderEnabled(String provider) {
	// TODO: implement onProviderEnabled
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO: implement onStatusChanged
    }

    /**
     * Remove updates, when need to pause work
     */
    public void pause() {
	locationManager.removeUpdates(this);
    }

    /**
     * Add updates after pause.
     */
    public void resume() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	provider = locationManager.getBestProvider(criteria, true);
	locationManager.requestLocationUpdates(provider, 1000, 1, this);
	lastLocation = this.locationManager.getLastKnownLocation(provider);
	if (lastLocation != null) {
	    updateLocation();
	}
    }

    /**
     * @return last known location
     */
    public Location getCurrentLocation() {
	return lastLocation;
    }

    /**
     * Calling when we get new location and want tell it to context
     */
    private void updateLocation() {
	context.updateLocation(lastLocation);
    }

    public boolean isLocationFixed() {
	return locationFixed;
    }

    /**
     * Set location fixed true, if we have some last location
     * 
     * @return true, if isLocationFixed set true
     */
    public boolean setLocationFixed() {
	if (lastLocation == null) {
	    return false;
	}
	locationFixed = true;
	return true;
    }

    /**
     * @return true if best provider enabled
     */
    public boolean isBestProviderEnabled() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	String bestProv = locationManager.getBestProvider(criteria, false);
	return locationManager.isProviderEnabled(bestProv);
    }

    public boolean isBestProviderGps() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	String bestProv = locationManager.getBestProvider(criteria, false);
	return bestProv.equals(LocationManager.GPS_PROVIDER);
    }
}
