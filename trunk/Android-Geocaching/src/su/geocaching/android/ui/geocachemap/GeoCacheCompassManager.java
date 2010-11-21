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

    private SensorManager sensorManager;
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
	isCompassAvailable = sensorManager == null;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
	int type = event.sensor.getType();
	int azimuth = 0;

	if (type == Sensor.TYPE_ORIENTATION) {
	    azimuth = (int) event.values[0];

	    if (azimuth != lastAzimuth) {
		lastAzimuth = azimuth;
		context.updateAzimuth(lastAzimuth);
	    }
	}
    }

    /**
     * Starting sensor
     */
    public void resume() {
	if (!isCompassAvailable) {
	    return;
	}
	Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	if (sensor == null) {
	    isCompassAvailable = false;
	    return;
	}
	sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
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
