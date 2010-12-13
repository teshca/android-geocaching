package su.geocaching.android.controller;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Sensor manager which calculate bearing of user
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 */
public class CompassManager implements SensorEventListener {

    private static final float RAD2DEG = (float) (180 / Math.PI);
    private SensorManager sensorManager;
    private float[] afGravity = new float[3];
    private float[] afGeomagnetic = new float[3];
    private float[] afRotation = new float[16];
    private float[] afInclination = new float[16];
    private float[] afOrientation = new float[3];
    private int lastBearing;
    private boolean isCompassAvailable;
    private List<ICompassAware> subsribers;

    /**
     * @param sensorManager
     *            manager which can add or remove updates of sensors
     */
    public CompassManager(SensorManager sensorManager) {
	this.sensorManager = sensorManager;
	isCompassAvailable = sensorManager != null;
	subsribers = new ArrayList<ICompassAware>();
    }

    /**
     * @param subsriber
     *            activity which will be listen location updates
     */
    public void addSubscriber(ICompassAware subsriber) {
	if (subsribers.size() == 0) {
	    addUpdates();
	}
	subsribers.add(subsriber);
    }

    /**
     * @param subsriber
     *            activity which no need to listen location updates
     * @return true if activity was subsribed on location updates
     */
    public boolean removeSubsriber(ICompassAware subsriber) {
	boolean res = subsribers.remove(subsriber);
	if (subsribers.size() == 0) {
	    removeUpdates();
	}
	return res;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.hardware.SensorEventListener#onSensorChanged(android.hardware
     * .SensorEvent)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
	int type = event.sensor.getType();
	float[] data;
	if (type == Sensor.TYPE_ACCELEROMETER) {
	    data = afGravity;
	} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
	    data = afGeomagnetic;
	} else {
	    // we do not handle this sensor type
	    return;
	}

	for (int i = 0; i < 3; i++)
	    data[i] = event.values[i];

	SensorManager.getRotationMatrix(afRotation, afInclination, afGravity, afGeomagnetic);
	SensorManager.getOrientation(afRotation, afOrientation);
	int lastBearingLocal = (int) (afOrientation[0] * RAD2DEG);
	if (lastBearingLocal != lastBearing) {
	    lastBearing = lastBearingLocal;
	    updateBearing(lastBearing);
	}
    }

    /**
     * @param lastBearing
     *            current bearing known to this listener
     */
    private void updateBearing(int lastBearing) {
	for (ICompassAware subscriber : subsribers) {
	    subscriber.updateBearing(lastBearing);
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
	sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI);
	sensorManager.registerListener(this, magnitudeSensor, SensorManager.SENSOR_DELAY_UI);
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
     * @return last known bearing
     */
    public int getLastBearing() {
	return lastBearing;
    }

    /**
     * @return true if we can calculate azimuth using hardware
     */
    public boolean isCompassAvailable() {
	return isCompassAvailable;
    }
}
