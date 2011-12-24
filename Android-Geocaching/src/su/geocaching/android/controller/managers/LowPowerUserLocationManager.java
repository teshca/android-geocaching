package su.geocaching.android.controller.managers;

import android.location.*;
import android.os.Bundle;

import java.util.*;

/**
 * Location manager which get updates of location by GSM/Wi-Fi
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 */
public class LowPowerUserLocationManager implements LocationListener {

    private static final String TAG = LowPowerUserLocationManager.class.getCanonicalName();

    private final LocationManager locationManager;
    private final HashSet<ILocationAware> subscribers;
    private final Criteria criteria;
    private String provider = null;

    private static final int MIN_UPDATE_TIME = 30000;  // 30 sec
    private static final int MIN_UPDATE_DISTANCE = 50; // 50 м

    /**
     * @param locationManager manager which can add or remove updates of location services
     */
    public LowPowerUserLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
        subscribers = new HashSet<ILocationAware>();

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.POWER_LOW);
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

    /*
     * (non-Javadoc)
     *
     * @see android.location.LocationListener#onLocationChanged(android.location. Location)
     */
    @Override
    public void onLocationChanged(Location location) {
        synchronized (subscribers) {
            LogManager.d(TAG, "Location changed: send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
            for (ILocationAware subscriber : subscribers) {
                subscriber.updateLocation(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Tell to subscribers about event using statuses
     *
     * @param provider which has been disabled
     */
    @Override
    public void onProviderDisabled(String provider) {
        //onAggregatedStatusChanged(provider, EVENT_PROVIDER_DISABLED, null);
    }

    /**
     * Tell to subscribers about event using statuses
     *
     * @param provider which has been enabled
     */
    @Override
    public void onProviderEnabled(String provider) {
        //onAggregatedStatusChanged(provider, EVENT_PROVIDER_ENABLED, null);
    }

    /**
     * Remove updates of location
     */
    private synchronized void removeUpdates() {
        LogManager.d(TAG, "remove location updates at " + Long.toString(System.currentTimeMillis()));
        locationManager.removeUpdates(this);
        provider = "none";
    }

    /**
     * Add updates of location
     */
    private synchronized void addUpdates() {
        provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
        } else {
            LogManager.w(TAG, "provider == null");
        }
        LogManager.d(TAG, "add updates. Provider is " + provider);
    }

    /**
     * @return true if best provider by accuracy locationAvailable
     */
    public boolean isBestProviderEnabled() {
        String bestProvider = locationManager.getBestProvider(criteria, false);
        return locationManager.isProviderEnabled(bestProvider);
    }
}
