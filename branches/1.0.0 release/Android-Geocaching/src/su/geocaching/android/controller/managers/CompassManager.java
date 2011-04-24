package su.geocaching.android.controller.managers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Sensor manager which calculate bearing of user
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 */
public class CompassManager implements SensorEventListener {

    private static final float RAD2DEG = (float) (180 / Math.PI);

    private float[] afGravity = new float[3];
    private float[] afGeomagnetic = new float[3];
    private float[] afRotation = new float[16];
    private float[] afInclination = new float[16];
    private float[] afOrientation = new float[3];

    private SensorManager sensorManager;
    private int lastDirection;
    private boolean isCompassAvailable;
    private List<ICompassAware> subscribers;

    /**
     * @param sensorManager manager which can add or remove updates of sensors
     */
    public CompassManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        isCompassAvailable = sensorManager != null;
        subscribers = new ArrayList<ICompassAware>();
    }

    /**
     * @param subscriber activity which will be listen location updates
     */
    public void addSubscriber(ICompassAware subscriber) {
        if (subscribers.size() == 0) {
            addUpdates();
        }
        subscribers.add(subscriber);
    }

    /**
     * @param subscriber activity which no need to listen location updates
     * @return true if activity was subscribed on location updates
     */
    public boolean removeSubscriber(ICompassAware subscriber) {
        boolean res = subscribers.remove(subscriber);
        if (subscribers.size() == 0) {
            removeUpdates();
        }
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
        for (ICompassAware observer : subscribers) {
            observer.updateBearing(lastDirection);
        }
    }

    /**
     * Add updates of sensors
     */
    private void addUpdates() {
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
    private void removeUpdates() {
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
}
