package su.geocaching.android.searchGeoCache;

import su.geocaching.android.view.SearchGeoCacheMap;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class SearchGeoCacheLocationManager implements LocationListener {

    private Context context;
    private LocationManager lmLocationManager;
    private Location lastLocation;

    public SearchGeoCacheLocationManager(Context context) {
	this.context = context;
	lmLocationManager = (LocationManager) context
		.getSystemService(Context.LOCATION_SERVICE);
	lastLocation = lmLocationManager
		.getLastKnownLocation(Context.LOCATION_SERVICE);
	if (lastLocation != null) {
	    updateLocation();
	}
	resume();
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
	lmLocationManager.removeUpdates(this);
    }

    /**
     * Add updates after pause.
     */
    public void resume() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	String provider = lmLocationManager.getBestProvider(criteria, true);
	lmLocationManager.requestLocationUpdates(provider, 1000, 1, this);
    }

    public Location getCurrentLocation() {
	return lastLocation;
    }

    private void updateLocation() {
	if (context instanceof SearchGeoCacheMap) {
	    float azimuth = ((SearchGeoCacheMap) context).getLastAzimuth();
	    ((SearchGeoCacheMap) context).updateUserOverlay(lastLocation, azimuth);
	}
    }
}
