package su.geocaching.android.ui.checkpoints;

import java.text.DecimalFormat;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.CoordinateHelper;
import su.geocaching.android.controller.compass.CompassHelper;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.controller.managers.UserLocationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

/**
 * Coordinate converter for waypoints
 * 
 * @author Nikita Bumakov
 * 
 */
public class CreateCheckpointActivity extends Activity {

    private static final String TAG = CreateCheckpointActivity.class.getCanonicalName();
    private static final String STEP_BY_STEP_TAB_ACTIVITY_FOLDER = "/CreateCheckpointActivity";

    private LinearLayout sexagesimal, sexagesimalSeconds, decimal, azimuth;
    private TextWatcher sexagesimalWatcher, sexagesimalSecondsWacher, decimalWatcher, azimuthWatcher;

    private EditText sLatDegrees, sLatMinutes, sLatSeconds;
    private EditText sLngDegrees, sLngMinutes, sLngSeconds;

    private EditText latDegrees, latMinutes, latmMinutes;
    private EditText lngDegrees, lngMinutes, lngmMinutes;

    private EditText dLatDegrees, latFraction;
    private EditText dLngDegrees, lngFraction;

    private EditText etAzimuth, etDistance;
    private GeoPoint currentLocation;

    private CheckpointManager checkpointManager;

    private GeoCache gc;
    private GeoPoint currentInputGeoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_checkpoint_activity);

        sexagesimal = (LinearLayout) findViewById(R.id.SexagesimalLayout);
        sexagesimalSeconds = (LinearLayout) findViewById(R.id.SexagesimalSeconsdLayout);
        decimal = (LinearLayout) findViewById(R.id.DecimalLayout);
        azimuth = (LinearLayout) findViewById(R.id.AzimuthLayout);

        gc = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());
        checkpointManager = Controller.getInstance().getCheckpointManager(gc.getId());

        sexagesimalWatcher = new SexagesimalListener();
        sexagesimalSecondsWacher = new SexagesimalSecondsListener();
        decimalWatcher = new DecimalListener();
        azimuthWatcher = new AzimuthListener();

        init();
        updateTextBoxes();
        startWatch();

        Toast.makeText(this, R.string.focus_hint, Toast.LENGTH_SHORT).show();

        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(STEP_BY_STEP_TAB_ACTIVITY_FOLDER);
    }

    private void init() {
        latDegrees = (EditText) findViewById(R.id.sLatDegrees);
        latMinutes = (EditText) findViewById(R.id.sLatMinutes);
        latmMinutes = (EditText) findViewById(R.id.sLatmMinutes);
        lngDegrees = (EditText) findViewById(R.id.sLngDegrees);
        lngMinutes = (EditText) findViewById(R.id.sLngMinutes);
        lngmMinutes = (EditText) findViewById(R.id.sLngmMinutes);

        sLatDegrees = (EditText) findViewById(R.id.ssLatDegrees);
        sLatMinutes = (EditText) findViewById(R.id.ssLatMinutes);
        sLatSeconds = (EditText) findViewById(R.id.ssLatSeconds);
        sLngDegrees = (EditText) findViewById(R.id.ssLngDegrees);
        sLngMinutes = (EditText) findViewById(R.id.ssLngMinutes);
        sLngSeconds = (EditText) findViewById(R.id.ssLngSeconds);

        dLatDegrees = (EditText) findViewById(R.id.dLatDegrees);
        latFraction = (EditText) findViewById(R.id.dLatFraction);
        dLngDegrees = (EditText) findViewById(R.id.dLngDegrees);
        lngFraction = (EditText) findViewById(R.id.dLngFraction);

        TextView info = (TextView) findViewById(R.id.tvAzimuthInputInfo);
        etAzimuth = (EditText) findViewById(R.id.azimuth);
        etDistance = (EditText) findViewById(R.id.distance);

        UserLocationManager locationManager = Controller.getInstance().getLocationManager();

        if (locationManager.hasLocation()) {
            currentLocation = CoordinateHelper.locationToGeoPoint(locationManager.getLastKnownLocation());
            info.setText(R.string.relative_to_current_location);
        } else {
            GeoCache gc = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache();
            currentLocation = gc.getLocationGeoPoint();
            info.setText(R.string.relative_to_cache_location);
        }

    }

    private void updateTextBoxes() {
        if (currentInputGeoPoint == null) {
            currentInputGeoPoint = gc.getLocationGeoPoint();
        }
        updateSexagesimal();
        updateSexagesimalSeconds();
        updateDecimal();
        updateAzimuth();
    }

    private void updateSexagesimal() {
        int lat = currentInputGeoPoint.getLatitudeE6();
        int lng = currentInputGeoPoint.getLongitudeE6();
        DecimalFormat format = new DecimalFormat("000");

        int[] sexagesimal = CoordinateHelper.coordinateE6ToSexagesimal(lat);
        latDegrees.setText(Integer.toString(sexagesimal[0]), BufferType.EDITABLE);
        latMinutes.setText(Integer.toString(sexagesimal[1]), BufferType.EDITABLE);
        latmMinutes.setText(format.format(sexagesimal[2]), BufferType.EDITABLE);

        sexagesimal = CoordinateHelper.coordinateE6ToSexagesimal(lng);
        lngDegrees.setText(Integer.toString(sexagesimal[0]), BufferType.EDITABLE);
        lngMinutes.setText(Integer.toString(sexagesimal[1]), BufferType.EDITABLE);
        lngmMinutes.setText(format.format(sexagesimal[2]), BufferType.EDITABLE);
    }

    private void updateSexagesimalSeconds() {
        int lat = currentInputGeoPoint.getLatitudeE6();
        int lng = currentInputGeoPoint.getLongitudeE6();
        DecimalFormat format = new DecimalFormat("00.000");

        float[] sSexagesimal = CoordinateHelper.coordinateE6ToSecSexagesimal(lat);
        sLatDegrees.setText(Integer.toString((int) sSexagesimal[0]), BufferType.EDITABLE);
        sLatMinutes.setText(Integer.toString((int) sSexagesimal[1]), BufferType.EDITABLE);
        sLatSeconds.setText(format.format(sSexagesimal[2]), BufferType.EDITABLE);

        sSexagesimal = CoordinateHelper.coordinateE6ToSecSexagesimal(lng);
        sLngDegrees.setText(Integer.toString((int) sSexagesimal[0]), BufferType.EDITABLE);
        sLngMinutes.setText(Integer.toString((int) sSexagesimal[1]), BufferType.EDITABLE);
        sLngSeconds.setText(format.format(sSexagesimal[2]), BufferType.EDITABLE);
    }

    private void updateDecimal() {
        int lat = currentInputGeoPoint.getLatitudeE6();
        int lng = currentInputGeoPoint.getLongitudeE6();
        DecimalFormat format = new DecimalFormat("000000");

        dLatDegrees.setText(Integer.toString(lat / 1000000), BufferType.EDITABLE);
        dLngDegrees.setText(Integer.toString(lng / 1000000), BufferType.EDITABLE);
        latFraction.setText(format.format(lat % 1000000), BufferType.EDITABLE);
        lngFraction.setText(format.format(lng % 1000000), BufferType.EDITABLE);
    }

    private void updateAzimuth() {
        etAzimuth.setText(CompassHelper.degreesToString(360 - CoordinateHelper.getBearingBetween(currentLocation, currentInputGeoPoint), "%.0f"), BufferType.EDITABLE);
        etDistance.setText(Integer.toString((int) CoordinateHelper.getDistanceBetween(currentLocation, currentInputGeoPoint)), BufferType.EDITABLE);
    }

    private void startWatch() {
        latDegrees.addTextChangedListener(sexagesimalWatcher);
        latMinutes.addTextChangedListener(sexagesimalWatcher);
        latmMinutes.addTextChangedListener(sexagesimalWatcher);
        lngDegrees.addTextChangedListener(sexagesimalWatcher);
        lngMinutes.addTextChangedListener(sexagesimalWatcher);
        lngmMinutes.addTextChangedListener(sexagesimalWatcher);

        sLatDegrees.addTextChangedListener(sexagesimalSecondsWacher);
        sLatMinutes.addTextChangedListener(sexagesimalSecondsWacher);
        sLatSeconds.addTextChangedListener(sexagesimalSecondsWacher);
        sLngDegrees.addTextChangedListener(sexagesimalSecondsWacher);
        sLngMinutes.addTextChangedListener(sexagesimalSecondsWacher);
        sLngSeconds.addTextChangedListener(sexagesimalSecondsWacher);

        dLatDegrees.addTextChangedListener(decimalWatcher);
        latFraction.addTextChangedListener(decimalWatcher);
        dLngDegrees.addTextChangedListener(decimalWatcher);
        lngFraction.addTextChangedListener(decimalWatcher);

        etAzimuth.addTextChangedListener(azimuthWatcher);
        etDistance.addTextChangedListener(azimuthWatcher);
    }

    private void stopWatch() {
        latDegrees.removeTextChangedListener(sexagesimalWatcher);
        latMinutes.removeTextChangedListener(sexagesimalWatcher);
        latmMinutes.removeTextChangedListener(sexagesimalWatcher);
        lngDegrees.removeTextChangedListener(sexagesimalWatcher);
        lngMinutes.removeTextChangedListener(sexagesimalWatcher);
        lngmMinutes.removeTextChangedListener(sexagesimalWatcher);

        sLatDegrees.removeTextChangedListener(sexagesimalSecondsWacher);
        sLatMinutes.removeTextChangedListener(sexagesimalSecondsWacher);
        sLatSeconds.removeTextChangedListener(sexagesimalSecondsWacher);
        sLngDegrees.removeTextChangedListener(sexagesimalSecondsWacher);
        sLngMinutes.removeTextChangedListener(sexagesimalSecondsWacher);
        sLngSeconds.removeTextChangedListener(sexagesimalSecondsWacher);

        dLatDegrees.removeTextChangedListener(decimalWatcher);
        latFraction.removeTextChangedListener(decimalWatcher);
        dLngDegrees.removeTextChangedListener(decimalWatcher);
        lngFraction.removeTextChangedListener(decimalWatcher);

        etAzimuth.removeTextChangedListener(azimuthWatcher);
        etDistance.removeTextChangedListener(azimuthWatcher);
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActvity(this);
    }

    public void onSexagesimalClick(View v) {
        toggleVisibility(sexagesimal);
    }

    public void onSexagesimalSecondsClick(View v) {
        toggleVisibility(sexagesimalSeconds);
    }

    public void onDecimalClick(View v) {
        toggleVisibility(decimal);
    }

    public void onAzimuthClick(View v) {
        toggleVisibility(azimuth);
    }

    private void toggleVisibility(View v) {
        switch (v.getVisibility()) {
            case View.VISIBLE:
                v.setVisibility(View.GONE);
                break;
            case View.GONE:
                v.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onEnterClick(View v) {
        try {
            checkpointManager.addCheckpoint(currentInputGeoPoint.getLatitudeE6(), currentInputGeoPoint.getLongitudeE6());
            finish();
        } catch (Exception e) {
            LogManager.e(TAG, e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
        }
    }

    class SexagesimalListener extends TextChangelListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int latitudeE6 = CoordinateHelper.sexagesimalToCoordinateE6(Integer.parseInt(latDegrees.getText().toString()), Integer.parseInt(latMinutes.getText().toString()),
                        Integer.parseInt(latmMinutes.getText().toString()));
                int longitudeE6 = CoordinateHelper.sexagesimalToCoordinateE6(Integer.parseInt(lngDegrees.getText().toString()), Integer.parseInt(lngMinutes.getText().toString()),
                        Integer.parseInt(lngmMinutes.getText().toString()));
                currentInputGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
                stopWatch();
                updateSexagesimalSeconds();
                updateDecimal();
                updateAzimuth();
                startWatch();
            } catch (Exception e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
    }

    class SexagesimalSecondsListener extends TextChangelListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int latitudeE6 = CoordinateHelper.secSexagesimalToCoordinateE6(Integer.parseInt(sLatDegrees.getText().toString()), Integer.parseInt(sLatMinutes.getText().toString()),
                        Float.parseFloat(sLatSeconds.getText().toString()));
                int longitudeE6 = CoordinateHelper.secSexagesimalToCoordinateE6(Integer.parseInt(sLngDegrees.getText().toString()), Integer.parseInt(sLngMinutes.getText().toString()),
                        Float.parseFloat(sLngSeconds.getText().toString()));
                currentInputGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
                stopWatch();
                updateSexagesimal();
                updateDecimal();
                updateAzimuth();
                startWatch();
            } catch (Exception e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
    }

    class DecimalListener extends TextChangelListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int latitudeE6 = Integer.parseInt(dLatDegrees.getText().toString()) * 1000000 + Integer.parseInt(latFraction.getText().toString());
                int longitudeE6 = Integer.parseInt(dLngDegrees.getText().toString()) * 1000000 + Integer.parseInt(lngFraction.getText().toString());
                currentInputGeoPoint = new GeoPoint(latitudeE6, longitudeE6);
                stopWatch();
                updateSexagesimal();
                updateSexagesimalSeconds();
                updateAzimuth();
                startWatch();
            } catch (Exception e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
    }

    class AzimuthListener extends TextChangelListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int azimuth = Integer.parseInt(etAzimuth.getText().toString());
                if (azimuth > 360) {
                    return;
                }
                int distance = Integer.parseInt(etDistance.getText().toString());
                GeoPoint goalGP = CoordinateHelper.distanceBearingToGeoPoint(currentLocation, azimuth, distance);
                currentInputGeoPoint = new GeoPoint(goalGP.getLatitudeE6(), goalGP.getLongitudeE6());
                stopWatch();
                updateDecimal();
                updateSexagesimal();
                updateSexagesimalSeconds();
                startWatch();
            } catch (Exception e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
    }

    class TextChangelListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
