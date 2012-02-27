package su.geocaching.android.controller.managers;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.HashSet;
import java.util.List;

/**
 * Location manager which get updates of location
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 */
public abstract class AbstractUserLocationManager implements LocationListener {

    private static final String TAG = AbstractUserLocationManager.class.getCanonicalName();

    private static final float AVERAGE_WALK_SPEED = 1f / 720; // 5 km/h = 5 * 1000 / 60 * 60 * 1000

    protected final LocationManager locationManager;
    protected final HashSet<ILocationAware> subscribers;
    protected final Criteria criteria;
    protected String provider = null;
    protected Location lastLocation;

    /**
     * @param locationManager manager which can add or remove updates of location services
     */
    public AbstractUserLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
        subscribers = new HashSet<ILocationAware>();
        criteria = new Criteria();
        lastLocation = calculateLastKnownLocation();
    }

    /**
     * Returns the most accurate and timely previously detected location.
     *
     * @return The most accurate and / or timely previously detected location.
     */
    protected Location calculateLastKnownLocation() {
        Location currentBestLocation = null;
        List<String> matchingProviders = locationManager.getAllProviders();

        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (isBetterLocation(location, currentBestLocation)) {
                currentBestLocation = location;
            }
        }
        return currentBestLocation;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();

        // Check whether the new location fix is more or less accurate
        float accuracyDelta = location.getAccuracy() - currentBestLocation.getAccuracy();

        if (accuracyDelta < 0) {
            if (timeDelta < 0) {
                // more accurate but older
                float speed = (accuracyDelta / timeDelta);
                return speed > AVERAGE_WALK_SPEED;
            } else {
                // more accurate and newer
                return true;
            }
        } else {
            if (timeDelta > 0) {
                // less accurate but newer
                float speed = (accuracyDelta / timeDelta);
                return speed < AVERAGE_WALK_SPEED;
            } else {
                // less accurate and older
                return false;
            }
        }
    }

    /**
     * @return true if last known location not null
     */
    public boolean hasLocation() {
        return lastLocation != null;
    }

    /**
     * @return last known location
     */
    public Location getLastKnownLocation() {
        return lastLocation;
    }

    /**
     * @param subscriber activity which will be listen location updates
     */
    public abstract void addSubscriber(ILocationAware subscriber);

    /**
     * @param subscriber activity which no need to listen location updates
     * @return true if activity was subscribed on location updates
     */
    public abstract boolean removeSubscriber(ILocationAware subscriber);

    /*
     * (non-Javadoc)
     *
     * @see android.location.LocationListener#onLocationChanged(android.location. Location)
     */
    public void onLocationChanged(Location location) {
        lastLocation = location;
        synchronized (subscribers) {
            LogManager.d(TAG, "Location changed: send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
            for (ILocationAware subscriber : subscribers) {
                subscriber.updateLocation(location);
            }
        }
    }

    /**
     * Remove updates of location
     */
    protected abstract void removeUpdates();

    /**
     * Add updates of location
     */
    protected abstract void addUpdates();

    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    /**
     * Tell to subscribers about event using statuses
     *
     * @param provider which has been disabled
     */
    public void onProviderDisabled(String provider) {
    }

    /**
     * Tell to subscribers about event using statuses
     *
     * @param provider which has been enabled
     */
    public void onProviderEnabled(String provider) {
    }

}
