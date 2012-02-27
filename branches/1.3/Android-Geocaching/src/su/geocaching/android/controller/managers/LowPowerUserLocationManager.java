package su.geocaching.android.controller.managers;

import android.location.*;

/**
 * Location manager which get updates of location by GSM/Wi-Fi
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 */
public class LowPowerUserLocationManager extends AbstractUserLocationManager implements LocationListener {

    private static final String TAG = LowPowerUserLocationManager.class.getCanonicalName();

    private static final int MIN_UPDATE_TIME = 30000;  // 30 sec
    private static final int MIN_UPDATE_DISTANCE = 50; // 50 м

    /**
     * @param locationManager manager which can add or remove updates of location services
     */
    public LowPowerUserLocationManager(LocationManager locationManager) {
        super(locationManager);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
    }

    /**
     * @param subscriber activity which will be listen location updates
     */
    public void addSubscriber(ILocationAware subscriber) {
        synchronized (subscribers) {
            if (subscribers.size() == 0) {
                addUpdates();
            }
            if (!subscribers.contains(subscriber)) {
                subscribers.add(subscriber);
            }
            LogManager.d(TAG, "	Count of subscribers became " + Integer.toString(subscribers.size()));
        }
    }

    /**
     * @param subscriber activity which no need to listen location updates
     * @return true if activity was subscribed on location updates
     */
    public boolean removeSubscriber(ILocationAware subscriber) {
        synchronized (subscribers) {
            boolean res = subscribers.remove(subscriber);
            LogManager.d(TAG, "remove subscriber. Count of subscribers became " + Integer.toString(subscribers.size()));
            if (subscribers.size() == 0) {
                removeUpdates();
            }
            return res;
        }
    }

    /**
     * Remove updates of location
     */
    protected synchronized void removeUpdates() {
        LogManager.d(TAG, "remove location updates at " + Long.toString(System.currentTimeMillis()));
        locationManager.removeUpdates(this);
        provider = null;
    }

    /**
     * Add updates of location
     */
    protected synchronized void addUpdates() {
        provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
        } else {
            LogManager.w(TAG, "provider == null");
        }
        LogManager.d(TAG, "add updates. Provider is " + provider);
    }
}
