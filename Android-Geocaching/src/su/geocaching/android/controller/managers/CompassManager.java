package su.geocaching.android.controller.managers;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import su.geocaching.android.controller.Controller;

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
    private UserLocationManager locationManager;
    private int lastDirection;
    private boolean isCompassAvailable;
    private List<IBearingAware> subscribers;

    /**
     * @param sensorManager manager which can add or remove updates of sensors
     */
    public CompassManager(SensorManager sensorManager, UserLocationManager userLocationManager) {
        this.sensorManager = sensorManager;
        this.locationManager = userLocationManager;
        isCompassAvailable = sensorManager != null;
        subscribers = new ArrayList<IBearingAware>();
        LogManager.d(TAG, "new CompassManager created");
    }

    /**
     * @param subscriber activity which will be listen location updates
     */
    public void addSubscriber(IBearingAware subscriber) {
        subscribers.add(subscriber);
        LogManager.d(TAG, "addSubscriber, size: " + subscribers.size());
    }

    /**
     * @param subscriber activity which no need to listen location updates
     * @return true if activity was subscribed on location updates
     */
    public boolean removeSubscriber(IBearingAware subscriber) {
        boolean res = subscribers.remove(subscriber);
        if (subscribers.size() == 0) {
            removeUpdates();
        }
        LogManager.d(TAG, "removeSubscriber, size: " + subscribers.size());
        return res;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
        int lastBearingLocal = (int) (afOrientation[0] * RAD2DEG);
        if (lastBearingLocal != lastDirection) {
            lastDirection = lastBearingLocal;
            notifyObservers(lastDirection);
        }
    }

    /**
     * @param lastDirection current direction known to this listener
     */
    private void notifyObservers(int lastDirection) {
        for (IBearingAware observer : subscribers) {
            observer.updateBearing(lastDirection);
        }
    }

    /**
     * Add updates of sensors
     */
    private synchronized void addSensorUpdates() {
        LogManager.d(TAG, "addSensorUpdates");
        if (!isCompassAvailable) {
            return;
        }
        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnitudeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (gravitySensor == null || magnitudeSensor == null) {
            isCompassAvailable = false;
            return;
        }
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnitudeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Remove updates of sensors
     */
    private synchronized void removeUpdates() {
        LogManager.d(TAG, "removeUpdates");
        locationManager.removeSubscriber(this);
        if (!isCompassAvailable) {
            return;
        }
        sensorManager.unregisterListener(this);
    }

    /**
     * @return last known direction
     */
    public int getLastDirection() {
        return lastDirection;
    }

    /**
     * @return true if we can calculate azimuth using hardware
     */
    public boolean isCompassAvailable() {
        return isCompassAvailable;
    }

    /**
     * Set mode of compass - use bearing from gps or hardware sensors(accelerometer and magnetic field)
     *
     * @param useGps true if using gps
     */
    public void setUsingGpsCompass(boolean useGps) {
        LogManager.d(TAG, "setUsingGpsCompass " + useGps);

        if (useGps) {
            removeUpdates();
            locationManager.addSubscriber(this, false);
        } else {
            addSensorUpdates();
            locationManager.removeSubscriber(this);
        }
    }

    @Override
    public void updateLocation(Location location) {
        lastDirection = (int) location.getBearing();
        notifyObservers(lastDirection);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProviderEnabled(String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProviderDisabled(String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
