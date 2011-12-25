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
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;
        List<String> matchingProviders = locationManager.getAllProviders();

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result.
        // If no result is found within accuracy, return the newest Location.
        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if (location.hasAccuracy() && accuracy < bestAccuracy) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }
        return bestResult;
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
