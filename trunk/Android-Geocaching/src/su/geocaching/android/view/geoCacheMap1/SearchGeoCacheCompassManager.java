package su.geocaching.android.view.geoCacheMap1;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SearchGeoCacheCompassManager implements SensorEventListener {

    private static final float RAD2DEG = (float) (180 / Math.PI);
    private SensorManager smSensorManager;
    private float[] afGravity = new float[3];
    private float[] afGeomagnetic = new float[3];
    private float[] afRotation = new float[16];
    private float[] afInclination = new float[16];
    private float[] afOrientation = new float[3];
    private IActivityWithCompass context;
    private float lastAzimuth;

    public SearchGeoCacheCompassManager(IActivityWithCompass context, SensorManager sensorManager) {
        this.context = context;
        smSensorManager = sensorManager;
        resume();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO: implement this method
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

        SensorManager.getRotationMatrix(afRotation, afInclination, afGravity,
                afGeomagnetic);
        SensorManager.getOrientation(afRotation, afOrientation);
        float loclastAzimuth = afOrientation[0] * RAD2DEG;
        loclastAzimuth = Math.round(loclastAzimuth);
        if (loclastAzimuth != lastAzimuth) {
            lastAzimuth = loclastAzimuth;
            context.updateAzimuth(lastAzimuth);
        }
    }

    public void resume() {
        Sensor gravitySensor = smSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnitudeSensor = smSensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        smSensorManager.registerListener(this, gravitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        smSensorManager.registerListener(this, magnitudeSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void pause() {
        smSensorManager.unregisterListener(this);
    }

    public float getLastAzimuth() {
        return lastAzimuth;
    }
}
