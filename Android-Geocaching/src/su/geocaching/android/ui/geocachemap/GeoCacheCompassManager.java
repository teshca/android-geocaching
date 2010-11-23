package su.geocaching.android.ui.geocachemap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 * @description Sensor manager which calculate azimuth of user
 */
public class GeoCacheCompassManager implements SensorEventListener {

    private static final float RAD2DEG = (float) (180 / Math.PI);
    private SensorManager sensorManager;
    private float[] afGravity = new float[3];
    private float[] afGeomagnetic = new float[3];
    private float[] afRotation = new float[16];
    private float[] afInclination = new float[16];
    private float[] afOrientation = new float[3];
    private ICompassAware context;
    private int lastAzimuth;
    private boolean isCompassAvailable;

    /**
     * @param context
     *            - Activity which use this sensor
     * @param sensorManager
     *            - sensor manager of context
     */
    public GeoCacheCompassManager(ICompassAware context, SensorManager sensorManager) {
	this.context = context;
	this.sensorManager = sensorManager;
	isCompassAvailable = sensorManager != null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /*
     * Calculate new azimuth using ACCELEROMETER and MAGNETIC_FIELD
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
	int loclastAzimuth = (int) (afOrientation[0] * RAD2DEG);
	if (loclastAzimuth != lastAzimuth) {
	    lastAzimuth = loclastAzimuth;
	    context.updateAzimuth(lastAzimuth);
	}
    }

    /**
     * Starting sensor
     */
    public void resume() {
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
     * Stop sensor work
     */
    public void pause() {
	if (!isCompassAvailable) {
	    return;
	}
	sensorManager.unregisterListener(this);
    }

    /**
     * @return last known azimuth
     */
    public int getLastBearing() {
	return lastAzimuth;
    }

    public boolean isCompassAvailable() {
	return isCompassAvailable;
    }
}
