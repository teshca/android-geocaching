package su.geocaching.android.controller.managers;

import android.hardware.*;
import android.location.Location;
import android.os.Bundle;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.compass.CompassSourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Sensor manager which calculate bearing of user
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 */
public class CompassManager implements SensorEventListener, ILocationAware {

    private static final String TAG = CompassManager.class.getCanonicalName();
    private static final float RAD2DEG = (float) (180 / Math.PI);

    private float[] afGravity = new float[3];
    private float[] afGeomagnetic = new float[3];
    private float[] afRotation = new float[16];
    private float[] afInclination = new float[16];
    private float[] afOrientation = new float[3];

    private SensorManager sensorManager;
    private AccurateUserLocationManager locationManager;
    private float lastDirection;
    private boolean isCompassAvailable;
    private final List<IBearingAware> subscribers;
    private boolean isUsingGps;

    private Sensor gravitySensor;
    private Sensor magnitudeSensor;

    /**
     * @param sensorManager
     *         manager which can add or remove updates of sensors
     */
    public CompassManager(SensorManager sensorManager, AccurateUserLocationManager userLocationManager) {
        this.sensorManager = sensorManager;
        this.locationManager = userLocationManager;

        if (sensorManager != null) {
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnitudeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        isCompassAvailable = gravitySensor != null && magnitudeSensor != null;

        subscribers = new ArrayList<IBearingAware>();
        LogManager.d(TAG, "new CompassManager created");
    }

    public boolean IsCompassAvailable() {
        return isCompassAvailable;
    }

    /**
     * @param subscriber
     *         activity which will be listen location updates
     */
    public void addSubscriber(IBearingAware subscriber) {
        synchronized (subscribers) {
            subscribers.add(subscriber);
            if (subscribers.size() == 1) {
                isUsingGps = GetDefaultGpsUsing();
                if (isUsingGps) {
                    locationManager.addSubscriber(this);
                } else {
                    addSensorUpdates();
                }
            }
            notifyObservers(lastDirection);
        }
        LogManager.d(TAG, "addSubscriber, size: " + subscribers.size());
    }

    /**
     * @param subscriber
     *         activity which no need to listen location updates
     * @return true if activity was subscribed on location updates
     */
    public boolean removeSubscriber(IBearingAware subscriber) {
        boolean res;
        synchronized (subscribers) {
            res = subscribers.remove(subscriber);
            if (subscribers.size() == 0) {
                if (isUsingGps) {
                    locationManager.removeSubscriber(this);
                } else {
                    removeSensorUpdates();
                }
            }
            LogManager.d(TAG, "removeSubscriber, size: " + subscribers.size());
        }
        return res;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                //LogManager.d(TAG, "onAccuracyChanged(TYPE_ACCELEROMETER): %d", accuracy);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                //LogManager.d(TAG, "onAccuracyChanged(TYPE_MAGNETIC_FIELD): %d", accuracy);
                break;
        }

        if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            //TODO: add some indication for user
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] data;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                data = afGravity;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                data = afGeomagnetic;
                break;
            default:
                return;
        }

        System.arraycopy(event.values, 0, data, 0, 3);

        SensorManager.getRotationMatrix(afRotation, afInclination, afGravity, afGeomagnetic);
        SensorManager.getOrientation(afRotation, afOrientation);
        float lastBearingLocal = (float) (afOrientation[0] * RAD2DEG);

        if (lastBearingLocal != lastDirection) {
            lastDirection = lastBearingLocal;
            notifyObservers(lastDirection);
        }
    }

    /**
     * @param lastDirection
     *         current direction known to this listener
     */
    private void notifyObservers(float lastDirection) {
        float screenDirection = lastDirection + Controller.getInstance().getScreenRotation();
        float declination = getDeclination();
        float realDirrection = screenDirection + declination;
        for (IBearingAware observer : subscribers) {
            observer.updateBearing(realDirrection, declination, isUsingGps ? CompassSourceType.GPS : CompassSourceType.SENSOR);
        }
    }

    private float getDeclination() {
        final Location location = this.locationManager.getLastKnownLocation();
        if (location == null) return 0;

        GeomagneticField geometricField = new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(),
                location.getTime());

        return geometricField.getDeclination();
    }

    /**
     * Add updates of sensors
     */
    private synchronized void addSensorUpdates() {
        LogManager.d(TAG, "addSensorUpdates");
        if (!isCompassAvailable) {
            return;
        }
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnitudeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Remove updates of sensors
     */
    private synchronized void removeSensorUpdates() {
        LogManager.d(TAG, "removeSensorUpdates");
        if (!isCompassAvailable) {
            return;
        }
        sensorManager.unregisterListener(this);
    }

    /**
     * Set mode of compass - use bearing from gps or hardware sensors(accelerometer and magnetic field).
     * If manager already using this 'provider' - do nothing.
     *
     * @param useGps
     *         true if using gps
     */
    void resetUpdates(boolean useGps) {
        LogManager.d(TAG, "resetUpdates=" + useGps);
        useGps = useGps || GetDefaultGpsUsing();
        if (useGps == isUsingGps) {
            // already using
            return;
        }
        if (useGps) {
            removeSensorUpdates();
            locationManager.addSubscriber(this);
        } else {
            locationManager.removeSubscriber(this);
            addSensorUpdates();
        }
        isUsingGps = useGps;

        notifyObservers(lastDirection);
    }

    private boolean GetDefaultGpsUsing() {
        return !isCompassAvailable || Controller.getInstance().getPreferencesManager().isUsingGpsCompassPreference();
    }

    @Override
    public void updateLocation(Location location) {
        lastDirection = (int) location.getBearing();
        notifyObservers(lastDirection);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { /* do nothing */ }
}
