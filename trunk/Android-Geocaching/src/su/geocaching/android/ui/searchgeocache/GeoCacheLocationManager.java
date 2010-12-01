package su.geocaching.android.ui.searchgeocache;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Location manager which get updates of location by GPS or GSM/Wi-Fi
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 */
public class GeoCacheLocationManager implements LocationListener {
    private static final int MIN_DISTANCE = 1; // in meters
    private static final int MIN_TIME = 1000; // in milliseconds
    private static final String TAG = GeoCacheLocationManager.class.getCanonicalName();
    private static final String TIMER_NAME = "remove location updates timer";
    private static final long REMOVE_UPDATES_DELAY = 30000; // in milliseconds

    private LocationManager locationManager;
    private Location lastLocation;
    private String provider;
    private List<ILocationAware> subsribers;
    private Timer removeUpdatesTimer;
    private RemoveUpdatesTask removeUpdatesTask;
    private boolean isUpdating;

    /**
     * @param locationManager
     *            manager which can add or remove updates of location services
     */
    public GeoCacheLocationManager(LocationManager locationManager) {
	this.locationManager = locationManager;
	subsribers = new ArrayList<ILocationAware>();
	provider = "none";
	isUpdating = false;
	removeUpdatesTimer = new Timer(TIMER_NAME);
	removeUpdatesTask = new RemoveUpdatesTask(this);
	Log.d(TAG, "Init");
    }

    /**
     * @param subsriber
     *            activity which will be listen location updates
     */
    public void addSubscriber(ILocationAware subsriber) {
	removeUpdatesTask.cancel();

	Log.d(TAG, "addSubscriber: remove task cancelled;\n	isUpdating=" + Boolean.toString(isUpdating) + ";\n	subscribers=" + Integer.toString(subsribers.size()));

	if ((subsribers.size() == 0) && (!isUpdating)) {
	    addUpdates();
	}
	if (!subsribers.contains(subsriber)) {
	    subsribers.add(subsriber);
	}
	Log.d(TAG, "	Count of subsribers became " + Integer.toString(subsribers.size()));
    }

    /**
     * @param subsriber
     *            activity which no need to listen location updates
     * @return true if activity was subsribed on location updates
     */
    public boolean removeSubsriber(ILocationAware subsriber) {
	boolean res = subsribers.remove(subsriber);
	if (!res) {
	    return res;
	}
	if (subsribers.size() == 0) {
	    removeUpdatesTask.cancel();
	    removeUpdatesTask = new RemoveUpdatesTask(this);
	    removeUpdatesTimer.schedule(removeUpdatesTask, REMOVE_UPDATES_DELAY);
	    Log.d(TAG, "none subscribers. wait " + Long.toString(REMOVE_UPDATES_DELAY / 1000) + " s from " + Long.toString(System.currentTimeMillis()));
	}
	Log.d(TAG, "remove subsriber. Count of subsribers became " + Integer.toString(subsribers.size()));
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
	Log.d(TAG, "Location changed: send msg to " + Integer.toString(subsribers.size()) + " activity(es)");
	for (ILocationAware subsriber : subsribers) {
	    if ((subsriber instanceof ICompassAware) && (!((ICompassAware) subsriber).isCompassAvailable())) {
		((ICompassAware) subsriber).updateBearing((int) location.getBearing());
		Log.d(TAG, "update location: send bearing to " + subsriber.getClass().getCanonicalName());
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
	Log.d(TAG, "Provider (" + provider + ") disabled: send msg to " + Integer.toString(subsribers.size()) + " activity(es)");
	for (ILocationAware subsriber : subsribers) {
	    subsriber.onProviderDisabled(provider);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {
	Log.d(TAG, "Provider (" + provider + ") enabled: send msg to " + Integer.toString(subsribers.size()) + " activity(es)");
	for (ILocationAware subsriber : subsribers) {
	    subsriber.onProviderEnabled(provider);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onStatusChanged(java.lang.String,
     * int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	Log.d(TAG, "Provider (" + provider + ") status changed (new status is " + Integer.toString(status) + "): send msg to " + Integer.toString(subsribers.size()) + " activity(es)");
	for (ILocationAware subsriber : subsribers) {
	    subsriber.onStatusChanged(provider, status, extras);
	}
    }

    /**
     * Remove updates of location
     */
    private synchronized void removeUpdates() {
	if (!isUpdating) {
	    Log.w(TAG, "updates already removed");
	}
	Log.d(TAG, "remove location updates at " + Long.toString(System.currentTimeMillis()));
	locationManager.removeUpdates(this);
	provider = "none";
	isUpdating = false;
    }

    /**
     * Add updates of location
     */
    private void addUpdates() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	provider = locationManager.getBestProvider(criteria, true);
	locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
	isUpdating = true;
	Log.d(TAG, "add updates. Provider is " + provider);
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
     * @return true if best provider by accuracy enabled
     */
    public boolean isBestProviderEnabled() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	String bestProv = locationManager.getBestProvider(criteria, false);
	return locationManager.isProviderEnabled(bestProv);
    }

    /**
     * @return true if best provider by accuracy is gps
     */
    public boolean isBestProviderGps() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	String bestProv = locationManager.getBestProvider(criteria, false);
	return bestProv.equals(LocationManager.GPS_PROVIDER);
    }

    /**
     * @return true if now Manager will be request updates from best provider by
     *         accuracy
     */
    public boolean enableBestProviderUpdates() {
	if (!isBestProviderEnabled()) {
	    return false;
	}
	Log.d(TAG, "request for enable best provider");
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	if (provider.equals(locationManager.getBestProvider(criteria, true))) {
	    Log.d(TAG, "	best provider (" + provider + ") already running");
	    return true;
	}
	provider = locationManager.getBestProvider(criteria, true);
	removeUpdates();
	locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
	Log.d(TAG, "request for enable best provider: enabled");
	isUpdating = true;
	return true;
    }

    /**
     * @return name of the best provider by accuracy on device
     */
    public String getBestProvider() {
	Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	return locationManager.getBestProvider(criteria, false);
    }

    /**
     * @return name of current location provider
     */
    public String getCurrentProvider() {
	return provider;
    }

    /**
     * task which remove updates from LocationManager
     * 
     * @author Grigory Kalabin. grigory.kalabin@gmail.com
     */
    private class RemoveUpdatesTask extends TimerTask {
	private GeoCacheLocationManager parent;

	/**
	 * @param parent
	 *            listener which want remove updates
	 */
	public RemoveUpdatesTask(GeoCacheLocationManager parent) {
	    this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	public void run() {

	    parent.removeUpdates();
	}
    }
}
