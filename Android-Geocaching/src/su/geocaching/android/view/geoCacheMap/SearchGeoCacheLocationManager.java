package su.geocaching.android.view.geoCacheMap;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class SearchGeoCacheLocationManager implements LocationListener {

    private IActivityWithLocation context;
    private LocationManager locationManager;
    private Location lastLocation;
    private String provider;

    public SearchGeoCacheLocationManager(IActivityWithLocation context,
	    LocationManager locationManager) {
	this.context = context;
	this.locationManager = locationManager;
    }

    /**
     * Update location obtained from LocationManager
     */
    @Override
    public void onLocationChanged(Location location) {
	lastLocation = location;
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

    public Location getCurrentLocation() {
	return lastLocation;
    }

    private void updateLocation() {
	context.updateLocation(lastLocation);
    }
}
