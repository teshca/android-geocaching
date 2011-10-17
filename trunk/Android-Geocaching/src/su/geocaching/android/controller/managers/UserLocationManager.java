package su.geocaching.android.controller.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.GpsUpdateFrequency;
import su.geocaching.android.ui.R;

/**
 * Location manager which get updates of location by GPS or GSM/Wi-Fi
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since fall, 2010
 */
public class UserLocationManager implements LocationListener, GpsStatus.Listener {
    private static final String TAG = UserLocationManager.class.getCanonicalName();
    private static final String REMOVE_UPDATES_TIMER_NAME = "remove location updates mapupdatetimer";
    private static final String DEPRECATE_LOCATION_TIMER_NAME = "waiting for location deprecation";
    private static final long REMOVE_UPDATES_DELAY = 30000; // in milliseconds
    public static final int PRECISE_LOCATION_MAX_TIME = 60 * 1000; // in milliseconds
    public static final float PRECISE_LOCATION_MAX_ACCURACY = 10f;

    private LocationManager locationManager;
    private Location lastLocation;
    private String provider;
    private List<ILocationAware> subscribers;
    private List<ILocationAware> statusSubscribers;
    private Timer removeUpdatesTimer;
    private DeprecateLocationNotifier deprecateLocationNotifier;
    private Timer deprecateLocationTimer;
    private RemoveUpdatesTask removeUpdatesTask;
    private boolean isUpdating, isUpdatingOdometer;
    private float odometerDistance;

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
        removeUpdatesTimer = new Timer(REMOVE_UPDATES_TIMER_NAME);
        removeUpdatesTask = new RemoveUpdatesTask(this);
        deprecateLocationTimer = new Timer(DEPRECATE_LOCATION_TIMER_NAME);
        deprecateLocationNotifier = new DeprecateLocationNotifier();
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

        if (((subscribers.size() == 0) && (!isUpdating)) || (!isUpdating)) {
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
        if (isUpdatingOdometer && lastLocation != null) {
            odometerDistance += CoordinateHelper.getDistanceBetween(location, lastLocation);
        }
        lastLocation = location;
        // start timer which notify about deprecation
        deprecateLocationNotifier.cancel();
        deprecateLocationNotifier = new DeprecateLocationNotifier();
        deprecateLocationTimer.schedule(deprecateLocationNotifier, PRECISE_LOCATION_MAX_TIME);
        LogManager.d(TAG, "Location changed: send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
        for (ILocationAware subscriber : subscribers) {
            subscriber.updateLocation(location);
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
        for (ILocationAware subscriber : subscribers) {
            subscriber.onProviderDisabled(provider);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {
        LogManager.d(TAG, "Provider (" + provider + ") locationAvailable: send msg to " + Integer.toString(subscribers.size()) + " activity(es)");
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
        for (ILocationAware subscriber : statusSubscribers) {
            subscriber.onStatusChanged(provider, status, extras);
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
     * Check is last known location actual or not
     * @return true, if last known location actual
     */
    public boolean hasPreciseLocation() {
        return hasLocation() && lastLocation.getTime() + PRECISE_LOCATION_MAX_TIME < System.currentTimeMillis()
                && lastLocation.hasAccuracy() && lastLocation.getAccuracy() < PRECISE_LOCATION_MAX_ACCURACY;
    }

    /**
     * @return true if last known location not null
     */
    public boolean hasLocation() {
        return lastLocation != null;
    }

    /**
     * @return true if best provider by accuracy locationAvailable
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
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (provider.equals(bestProvider)) {
            LogManager.d(TAG, "	best provider (" + provider + ") already running");
            return true;
        }
        removeUpdates();
        provider = bestProvider;
        requestLocationUpdates();
        LogManager.d(TAG, "request for enable best provider: locationAvailable");
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
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
            isUpdating = true;
        } else {
            LogManager.w(TAG, "provider == null");
        }
    }

    /**
     * @return name of the best provider by accuracy on device
     */
    public String getBestProvider(boolean isEnable) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        return locationManager.getBestProvider(criteria, isEnable);
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
        updateFrequency(Controller.getInstance().getPreferencesManager().getGpsUpdateFrequency());
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
   * Return the distance in meters after last odometer refresh
   *
   * @return distance in meters
   */
    public float getOdometerDistance() {
        return odometerDistance;
    }

   /**
    *  Refresh the odometer distance value
    */
    public void refreshOdometer() {
        odometerDistance = 0;
    }

  /**
   * Enable/Disable odometer
   *
   * @param isUpdating - flag Enable/Disable updating
   */
    public void setUpdatingOdometer(boolean isUpdating) {
        isUpdatingOdometer = isUpdating;
    }

   /**
    * Is odometer updating
    *
    * @return isUpdatingOdometer
    */
    public boolean isUpdatingOdometer() {
        return isUpdatingOdometer;
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
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
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

    /**
     * Task which notify about location deprecation
     *
     * @author Grigory Kalabin. grigory.kalabin@gmail.com
     */
    private class DeprecateLocationNotifier extends TimerTask {
        public DeprecateLocationNotifier() {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.TimerTask#run()
         */
        public void run() {
            Controller.getInstance().getCallbackManager().postEmptyMessage(CallbackManager.WHAT_LOCATION_DEPRECATED);
        }
    }
}
