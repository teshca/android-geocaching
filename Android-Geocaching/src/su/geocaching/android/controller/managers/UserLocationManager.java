package su.geocaching.android.controller.managers;

import android.location.*;
import android.os.Bundle;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.ui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Location manager which get updates of location by GPS or GSM/Wi-Fi
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 */
public class UserLocationManager implements LocationListener, GpsStatus.Listener {
    private static final String TAG = UserLocationManager.class.getCanonicalName();
    private static final String TIMER_NAME = "remove location updates mapupdatetimer";
    private static final long REMOVE_UPDATES_DELAY = 30000; // in milliseconds
    private static final float MAX_SPEED_OF_HARDWARE_COMPASS = 20 * 1000 / 3600; // (in m/s) if user speed lower than this - use hardware compass otherwise use GPS compass

    private LocationManager locationManager;
    private Location lastLocation;
    private String provider;
    private List<ILocationAware> subscribers;
    private List<ILocationAware> statusSubscribers;
    private Timer removeUpdatesTimer;
    private RemoveUpdatesTask removeUpdatesTask;
    private boolean isUpdating;

    private GpsUpdateFrequency updateFrequency;

    /**
     * @param locationManager manager which can add or remove updates of location services
     */
    public UserLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
        lastLocation = lastKnownLocation();
        updateFrequency = Controller.getInstance().getPreferencesManager().getGpsUpdateFrequency();
        subscribers = new ArrayList<ILocationAware>();
        statusSubscribers = new ArrayList<ILocationAware>();
        provider = "none";
        isUpdating = false;
        removeUpdatesTimer = new Timer(TIMER_NAME);
        removeUpdatesTask = new RemoveUpdatesTask(this);
        LogManager.d(TAG, "Init");
    }

    private Location lastKnownLocation() {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > 100 && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (time < 100 &&
                        bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }
        return bestResult;
    }

    /**
     * @param subscriber activity which will be listen location updates
     * @param withStatus true if subscriber want to recieve information about gps status
     */
    public void addSubscriber(ILocationAware subscriber, boolean withStatus) {
        removeUpdatesTask.cancel();

        LogManager.d(TAG, "addSubscriber: remove task cancelled;\n	isUpdating=" + Boolean.toString(isUpdating) + ";\n	subscribers=" + Integer.toString(subscribers.size()));

        if ((subscribers.size() == 0) && (!isUpdating)) {
            addUpdates();
        }
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
        if (withStatus && statusSubscribers.size() == 0) {
            addGpsStatusUpdates();
        }
        if (withStatus && !statusSubscribers.contains(subscriber)) {
            statusSubscribers.add(subscriber);
        }
        LogManager.d(TAG, "	Count of subscribers became " + Integer.toString(subscribers.size()));
    }

    /**
     * @param subscriber activity which no need to listen location updates
     * @return true if activity was subscribed on location updates
     */
    public boolean removeSubscriber(ILocationAware subscriber) {
        boolean res = subscribers.remove(subscriber);
        statusSubscribers.remove(subscriber);
        if (subscribers.size() == 0 && res) {
            removeUpdatesTask.cancel();
            removeUpdatesTask = new RemoveUpdatesTask(this);
            removeUpdatesTimer.schedule(removeUpdatesTask, REMOVE_UPDATES_DELAY);
            LogManager.d(TAG, "none subscribers. wait " + Long.toString(REMOVE_UPDATES_DELAY / 1000) + " s from " + Long.toString(System.currentTimeMillis()));
        }
        if (statusSubscribers.size() == 0) {
            removeGpsStatusUpdates();
        }
        LogManager.d(TAG, "remove subscriber. Count of subscribers became " + Integer.toString(subscribers.size()));
        return res;
    }

    /**
     * Remove updates of gps status. Location updates remains
     *
     * @param subscriber activity which no need to listen gps status updates
     */
    public void removeStatusListening(ILocationAware subscriber) {
        statusSubscribers.remove(subscriber);
        if (statusSubscribers.size() == 0) {
            removeGpsStatusUpdates();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onLocationChanged(android.location. Location)
     */
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LogManager.d(TAG, "Location changed: send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
        boolean isCompassAvailable = Controller.getInstance().getCompassManager().isCompassAvailable();
        for (ILocationAware subsriber : subscribers) {
            if ((subsriber instanceof IBearingAware) && (!isCompassAvailable)) {
                ((IBearingAware) subsriber).updateBearing((int) location.getBearing());
                LogManager.d(TAG, "update location: send bearing to " + subsriber.getClass().getCanonicalName());
            }
            subsriber.updateLocation(location);
        }
        if (location.getSpeed() > MAX_SPEED_OF_HARDWARE_COMPASS) {
            Controller.getInstance().getCompassManager().setUsingGpsCompass(true);
        } else {
            Controller.getInstance().getCompassManager().setUsingGpsCompass(false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider) {
        LogManager.d(TAG, "Provider (" + provider + ") disabled: send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
        for (ILocationAware subsriber : subscribers) {
            subsriber.onProviderDisabled(provider);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {
        LogManager.d(TAG, "Provider (" + provider + ") enabled: send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
        for (ILocationAware subsriber : subscribers) {
            subsriber.onProviderEnabled(provider);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LogManager.d(TAG, "Provider (" + provider + ") status changed (new status is " + Integer.toString(status) + "): send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
        for (ILocationAware subsriber : statusSubscribers) {
            subsriber.onStatusChanged(provider, status, extras);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.GpsStatus.Listener#onGpsStatusChanged(int)
     */
    @Override
    public void onGpsStatusChanged(int arg0) {
        onStatusChanged(provider, LocationProvider.AVAILABLE, null);
    }

    /**
     * Remove updates of location
     */
    private synchronized void removeUpdates() {
        if (!isUpdating) {
            LogManager.w(TAG, "updates already removed");
        }
        LogManager.d(TAG, "remove location updates at " + Long.toString(System.currentTimeMillis()));
        locationManager.removeUpdates(this);
        provider = "none";
        isUpdating = false;
        Controller.getInstance().getCompassManager().setUsingGpsCompass(false);
    }

    /**
     * Add updates of location
     */
    private void addUpdates() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, true);
        requestLocationUpdates();
        LogManager.d(TAG, "add updates. Provider is " + provider);
    }

    /**
     * Add updates of status gps engine
     */
    private void addGpsStatusUpdates() {
        locationManager.addGpsStatusListener(this);
    }

    /**
     * remove updates of gps engine status
     */
    private void removeGpsStatusUpdates() {
        locationManager.removeGpsStatusListener(this);
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
    public boolean hasLocation() {
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
     * @return true if now Manager will be request updates from best provider by accuracy
     */
    public boolean enableBestProviderUpdates() {
        if (!isBestProviderEnabled()) {
            return false;
        }
        LogManager.d(TAG, "request for enable best provider");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        if (provider.equals(locationManager.getBestProvider(criteria, true))) {
            LogManager.d(TAG, "	best provider (" + provider + ") already running");
            return true;
        }
        provider = locationManager.getBestProvider(criteria, true);
        removeUpdates();
        requestLocationUpdates();
        LogManager.d(TAG, "request for enable best provider: enabled");
        return true;
    }

    /**
     * call request location updates on location manager with right min time and min distance
     */
    private void requestLocationUpdates() {
        long minTime;
        float minDistance;
        switch (updateFrequency) {
            case MINIMAL:
                minTime = 16000;
                minDistance = 16;
                break;
            case RARELY:
                minTime = 8000;
                minDistance = 8;
                break;
            case NORMAL:
                minTime = 4000;
                minDistance = 4;
                break;
            case OFTEN:
                minTime = 2000;
                minDistance = 2;
                break;
            case MAXIMAL:
                minTime = 1000;
                minDistance = 1;
                break;
            default:
                minTime = 4000;
                minDistance = 4;
                break;
        }
        LogManager.d(TAG, "update frequency: " + updateFrequency.toString());
        locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
        isUpdating = true;
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
     * Refresh frequency of location updates and re-request location updates from current provider
     *
     * @param value frequency which need
     */
    public synchronized void updateFrequency(GpsUpdateFrequency value) {
        if (updateFrequency.equals(value)) {
            LogManager.d(TAG, "refresh frequency: already done");
            return;
        }
        updateFrequency = value;
        LogManager.d(TAG, "refresh frequency. new value is " + updateFrequency.toString());
        if (isUpdating) {
            removeUpdates();
            addUpdates();
            LogManager.d(TAG, "refresh frequency: re-request location updates from provider");
        }
    }

    /**
     * Set frequency from preferences of location updates and re-request location updates from current provider
     */
    public synchronized void updateFrequencyFromPreferences() {
        GpsUpdateFrequency prefsFrequency = Controller.getInstance().getPreferencesManager().getGpsUpdateFrequency();
        if (updateFrequency.equals(prefsFrequency)) {
            LogManager.d(TAG, "refresh frequency from prefs: already done");
            return;
        }
        updateFrequency = prefsFrequency;
        LogManager.d(TAG, "refresh frequency. new value is " + updateFrequency.toString());
        if (isUpdating) {
            removeUpdates();
            addUpdates();
            LogManager.d(TAG, "refresh frequency: re-request location updates from provider");
        }
    }

    /**
     * Return string like "satellites: 2/5" with info about satellites
     *
     * @return string with localized info about satellites
     */
    public String getSatellitesStatusString() {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        int usedInFix = 0;
        int count = 0;
        if (gpsStatus.getSatellites() == null) {
            return null;
        }
        for (GpsSatellite satellite : gpsStatus.getSatellites()) {
            count++;
            if (satellite.usedInFix()) {
                usedInFix++;
            }
        }
        return String.format("%s %d/%d", Controller.getInstance().getResourceManager().getString(R.string.gps_status_satellite_status), usedInFix, count);
    }

    /**
     * task which remove updates from LocationManager
     *
     * @author Grigory Kalabin. grigory.kalabin@gmail.com
     */
    private class RemoveUpdatesTask extends TimerTask {
        private UserLocationManager parent;

        /**
         * @param parent listener which want remove updates
         */
        public RemoveUpdatesTask(UserLocationManager parent) {
            this.parent = parent;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.TimerTask#run()
         */
        public void run() {
            if (parent.isUpdating) {
                parent.removeUpdates();
            }
        }
    }
}
