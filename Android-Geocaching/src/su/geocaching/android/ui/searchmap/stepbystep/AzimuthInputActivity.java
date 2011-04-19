package su.geocaching.android.ui.searchmap.stepbystep;

import su.geocaching.android.controller.CheckpointManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.controller.GpsHelper;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.compass.CompassHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class AzimuthInputActivity extends Activity {

    private static final String TAG = SexagesimalInputActivity.class.getCanonicalName();
    private static final String AZIMUTH_INPUT_ACTIVITY_FOLDER = "/AzimuthInputActivity";
    private EditText etAzimuth, etDistance;
    private GeoPoint currentLocation;
    private CheckpointManager checkpointManager;
    private TextWatcher textWacher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.azimut_input);

        TextView info = (TextView) findViewById(R.id.tvAzimuthInputInfo);
        etAzimuth = (EditText) findViewById(R.id.azimuth);
        etDistance = (EditText) findViewById(R.id.distance);

        GeoCacheLocationManager locationManager = Controller.getInstance().getLocationManager();

        if (locationManager.hasLocation()) {
            currentLocation = GpsHelper.locationToGeoPoint(locationManager.getLastKnownLocation());
            info.setText(R.string.relative_to_current_location);
        } else {
            GeoCache gc = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache();
            currentLocation = gc.getLocationGeoPoint();
            info.setText(R.string.relative_to_cache_location);
        }
        textWacher = new TextChangeListener();
        
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(AZIMUTH_INPUT_ACTIVITY_FOLDER);
    }

    @Override
    protected void onResume() {
        GeoCache gc = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());
        checkpointManager = Controller.getInstance().getCheckpointManager(gc.getId());

        GeoPoint gp;
        if ((gp = checkpointManager.getLastInputGeoPoint()) == null) {
            gp = gc.getLocationGeoPoint();
        } else {
            etAzimuth.setText(CompassHelper.degreesToString(360 - GpsHelper.getBearingBetween(currentLocation, gp), "%.0f"), BufferType.EDITABLE);
            etDistance.setText(Integer.toString((int) GpsHelper.getDistanceBetween(currentLocation, gp)), BufferType.EDITABLE);
        }
        etAzimuth.addTextChangedListener(textWacher);
        etDistance.addTextChangedListener(textWacher);
        super.onResume();
    }

    @Override
    protected void onPause() {
        etAzimuth.removeTextChangedListener(textWacher);
        etDistance.removeTextChangedListener(textWacher);
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            checkpointManager.setLastInputGeoPoint(null);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onEnterClick(View v) {
        int azimuth = Integer.parseInt(this.etAzimuth.getText().toString());
        if (azimuth > 360) {
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
            return;
        }
        int distance = Integer.parseInt(this.etDistance.getText().toString());
        GeoPoint goalGP = GpsHelper.distanceBearingToGeoPoint(currentLocation, azimuth, distance);
        checkpointManager.addCheckpoint(goalGP.getLatitudeE6(), goalGP.getLongitudeE6());
        checkpointManager.setLastInputGeoPoint(null);
        finish();
    }

    class TextChangeListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int azimuth = Integer.parseInt(etAzimuth.getText().toString());
                if (azimuth > 360) {
                    return;
                }
                int distance = Integer.parseInt(etDistance.getText().toString());
                GeoPoint goalGP = GpsHelper.distanceBearingToGeoPoint(currentLocation, azimuth, distance);
                checkpointManager.setLastInputGeoPoint(new GeoPoint(goalGP.getLatitudeE6(), goalGP.getLongitudeE6()));
            } catch (Exception e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
