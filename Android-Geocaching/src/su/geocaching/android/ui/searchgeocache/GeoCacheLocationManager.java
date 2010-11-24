package su.geocaching.android.ui.searchgeocache;

import java.util.ArrayList;
import java.util.List;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 *        <p>
 *        Location manager which get updates of location by GPS or GSM/Wi-Fi
 *        </p>
 */
public class GeoCacheLocationManager implements LocationListener {
    private static final int MIN_DISTANCE = 1; // in meters
    private static final int MIN_TIME = 1000; // in milliseconds

    private LocationManager locationManager;
    private Location lastLocation;
    private String provider;
    private List<ILocationAware> subsribers;

    /**
     * @param locationManager
     *            manager which can add or remove updates of location services
     */
    public GeoCacheLocationManager(LocationManager locationManager) {
	this.locationManager = locationManager;
	subsribers = new ArrayList<ILocationAware>();
	provider = "none";
    }

    /**
     * @param subsriber
     *            activity which will be listen location updates
     */
    public void addSubscriber(ILocationAware subsriber) {
	if (subsribers.size() == 0) {
	    addUpdates();
	}
	if (!subsribers.contains(subsriber)) {
	    subsribers.add(subsriber);
	}
    }

    /**
     * @param subsriber
     *            activity which no need to listen location updates
     * @return true if activity was subsribed on location updates
     */
    public boolean removeSubsriber(ILocationAware subsriber) {
	boolean res = subsribers.remove(subsriber);
	if (subsribers.size() == 0) {
	    removeUpdates();
	}
	return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onLocationChanged(android.location.
     * Location)
     */
    @Override
    public void onLocationChanged(Location location) {
	lastLocation = location;
	for (ILocationAware subsriber : subsribers) {
	    if ((subsriber instanceof ICompassAware) && (!((ICompassAware) subsriber).isCompassAvailable())) {
		((ICompassAware) subsriber).updateBearing((int) location.getBearing());
	    }
	    subsriber.updateLocation(location);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider) {
	// TODO: implement onProviderDisabled
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {
	// TODO: implement onProviderEnabled
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onStatusChanged(java.lang.String,
     * int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO: implement onStatusChanged
    }

    /**
     * Remove updates of location
     */
    private void removeUpdates() {
	locationManager.removeUpdates(this);
    }

    /**
     * Add updates of location
     */
    private void addUpdates() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	provider = locationManager.getBestProvider(criteria, true);
	locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
	if (lastLocation != null) {
	    onLocationChanged(lastLocation);
	}
    }

    /**
     * @return last known location
     */
    public Location getLastKnownLocation() {
	return lastLocation;
    }

    /**
     * @return true if last known location not null
     */
    public boolean isLocationFixed() {
	return lastLocation != null;
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

    /**
     * @return true if best provider is gps
     */
    public boolean isBestProviderGps() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	String bestProv = locationManager.getBestProvider(criteria, false);
	return bestProv.equals(LocationManager.GPS_PROVIDER);
    }
}
