package su.geocaching.android.ui.checkpoints;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.utils.Sexagesimal;
import su.geocaching.android.controller.utils.SexagesimalSec;
import su.geocaching.android.controller.compass.CompassHelper;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.controller.managers.AccurateUserLocationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;

import java.text.DecimalFormat;

/**
 * Coordinate converter for waypoints
 *
 * @author Nikita Bumakov
 */
public class CreateCheckpointActivity extends SherlockActivity {

    private static final String TAG = CreateCheckpointActivity.class.getCanonicalName();
    private static final String CREATE_CHECK_POINT_ACTIVITY_NAME = "/CreateCheckpointActivity";

    private LinearLayout sexagesimal, sexagesimalSeconds, decimal, azimuth;
    private TextWatcher sexagesimalWatcher, sexagesimalSecondsWatcher, decimalWatcher, azimuthWatcher;

    private EditText name;

    private EditText sLatDegrees, sLatMinutes, sLatSeconds;
    private EditText sLngDegrees, sLngMinutes, sLngSeconds;

    private EditText latDegrees, latMinutes, latMinutesFraction;
    private EditText lngDegrees, lngMinutes, lngMinutesFraction;

    private EditText dLatDegrees, dLatDegreesFraction;
    private EditText dLngDegrees, dLngDegreesFraction;

    private EditText etAzimuth, etDistance;
    private GeoPoint currentLocation;

    private CheckpointManager checkpointManager;

    private GeoCache geoCache;
    private GeoPoint currentInputGeoPoint;

    private DecimalFormat degreesFractionFormat = new DecimalFormat("000000");
    private DecimalFormat minutesFractionFormat = new DecimalFormat("000");
    private DecimalFormat minFormat = new DecimalFormat("00");
    private DecimalFormat secFormat = new DecimalFormat("00.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_checkpoint_activity);

        geoCache = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());
        checkpointManager = Controller.getInstance().getCheckpointManager(geoCache.getId());

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(geoCache.getName());

        sexagesimal = (LinearLayout) findViewById(R.id.SexagesimalLayout);
        sexagesimalSeconds = (LinearLayout) findViewById(R.id.SexagesimalSeconsdLayout);
        decimal = (LinearLayout) findViewById(R.id.DecimalLayout);
        azimuth = (LinearLayout) findViewById(R.id.AzimuthLayout);

        sexagesimalWatcher = new SexagesimalListener();
        sexagesimalSecondsWatcher = new SexagesimalSecondsListener();
        decimalWatcher = new DecimalListener();
        azimuthWatcher = new AzimuthListener();

        init();
        updateTextBoxes();
        startWatch();

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(CREATE_CHECK_POINT_ACTIVITY_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.create_checkpoint_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavigationManager.startDashboardActivity(this);
                return true;
            case R.id.menu_save_checkpoint:
                onSaveCheckpoint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {

        name = (EditText) findViewById(R.id.checkpointName);

        latDegrees = (EditText) findViewById(R.id.sLatDegrees);
        latMinutes = (EditText) findViewById(R.id.sLatMinutes);
        latMinutesFraction = (EditText) findViewById(R.id.sLatmMinutes);
        lngDegrees = (EditText) findViewById(R.id.sLngDegrees);
        lngMinutes = (EditText) findViewById(R.id.sLngMinutes);
        lngMinutesFraction = (EditText) findViewById(R.id.sLngmMinutes);

        sLatDegrees = (EditText) findViewById(R.id.ssLatDegrees);
        sLatMinutes = (EditText) findViewById(R.id.ssLatMinutes);
        sLatSeconds = (EditText) findViewById(R.id.ssLatSeconds);
        sLngDegrees = (EditText) findViewById(R.id.ssLngDegrees);
        sLngMinutes = (EditText) findViewById(R.id.ssLngMinutes);
        sLngSeconds = (EditText) findViewById(R.id.ssLngSeconds);

        dLatDegrees = (EditText) findViewById(R.id.dLatDegrees);
        dLatDegreesFraction = (EditText) findViewById(R.id.dLatFraction);
        dLngDegrees = (EditText) findViewById(R.id.dLngDegrees);
        dLngDegreesFraction = (EditText) findViewById(R.id.dLngFraction);

        TextView info = (TextView) findViewById(R.id.tvAzimuthInputInfo);
        etAzimuth = (EditText) findViewById(R.id.azimuth);
        etDistance = (EditText) findViewById(R.id.distance);

        AccurateUserLocationManager locationManager = Controller.getInstance().getLocationManager();

        if (locationManager.hasLocation()) {
            currentLocation = CoordinateHelper.locationToGeoPoint(locationManager.getLastKnownLocation());
            info.setText(R.string.relative_to_current_location);
        } else {
            // GeoCache geoCache = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache();
            GeoCache gc = Controller.getInstance().getDbManager().getCacheByID(this.geoCache.getId());
            currentLocation = gc.getLocationGeoPoint();
            info.setText(R.string.relative_to_cache_location);
        }

    }

    private void updateTextBoxes() {
        if (currentInputGeoPoint == null) {
            currentInputGeoPoint = geoCache.getLocationGeoPoint();
        }
        updateSexagesimal();
        updateSexagesimalSeconds();
        updateDecimal();
        updateAzimuth();
    }

    private void updateSexagesimal() {
        int lat = currentInputGeoPoint.getLatitudeE6();
        int lng = currentInputGeoPoint.getLongitudeE6();

        Sexagesimal sexagesimal = new Sexagesimal(lat).roundTo(3);
        latDegrees.setText(Integer.toString(sexagesimal.degrees), BufferType.EDITABLE);
        int minutesE3 = (int) Math.round(sexagesimal.minutes * 1000);
        latMinutes.setText(minFormat.format(minutesE3 / 1000), BufferType.EDITABLE);
        latMinutesFraction.setText(minutesFractionFormat.format(minutesE3 % 1000), BufferType.EDITABLE);

        sexagesimal = new Sexagesimal(lng).roundTo(3);
        lngDegrees.setText(Integer.toString(sexagesimal.degrees), BufferType.EDITABLE);
        minutesE3 = (int) Math.round((sexagesimal.minutes * 1000));
        lngMinutes.setText(minFormat.format(minutesE3 / 1000), BufferType.EDITABLE);
        lngMinutesFraction.setText(minutesFractionFormat.format(minutesE3 % 1000), BufferType.EDITABLE);
    }

    private void updateSexagesimalSeconds() {
        int lat = currentInputGeoPoint.getLatitudeE6();
        int lng = currentInputGeoPoint.getLongitudeE6();

        SexagesimalSec sSexagesimal = new SexagesimalSec(lat).roundTo(2);
        sLatDegrees.setText(Integer.toString(sSexagesimal.degrees), BufferType.EDITABLE);
        sLatMinutes.setText(minFormat.format(sSexagesimal.minutes), BufferType.EDITABLE);
        sLatSeconds.setText(secFormat.format(sSexagesimal.seconds), BufferType.EDITABLE);

        sSexagesimal = new SexagesimalSec(lng).roundTo(2);
        sLngDegrees.setText(Integer.toString(sSexagesimal.degrees), BufferType.EDITABLE);
        sLngMinutes.setText(minFormat.format(sSexagesimal.minutes), BufferType.EDITABLE);
        sLngSeconds.setText(secFormat.format(sSexagesimal.seconds), BufferType.EDITABLE);
    }

    private void updateDecimal() {
        int lat = currentInputGeoPoint.getLatitudeE6();
        int lng = currentInputGeoPoint.getLongitudeE6();

        dLatDegrees.setText(Integer.toString(lat / 1000000), BufferType.EDITABLE);
        dLngDegrees.setText(Integer.toString(lng / 1000000), BufferType.EDITABLE);
        dLatDegreesFraction.setText(degreesFractionFormat.format(lat % 1000000), BufferType.EDITABLE);
        dLngDegreesFraction.setText(degreesFractionFormat.format(lng % 1000000), BufferType.EDITABLE);
    }

    private void updateAzimuth() {
        etAzimuth.setText(CompassHelper.degreesToString(360 - CoordinateHelper.getBearingBetween(currentLocation, currentInputGeoPoint), "%.0f"), BufferType.EDITABLE);
        etDistance.setText(Integer.toString((int) CoordinateHelper.getDistanceBetween(currentLocation, currentInputGeoPoint)), BufferType.EDITABLE);
    }

    private void startWatch() {
        latDegrees.addTextChangedListener(sexagesimalWatcher);
        latMinutes.addTextChangedListener(sexagesimalWatcher);
        latMinutesFraction.addTextChangedListener(sexagesimalWatcher);
        lngDegrees.addTextChangedListener(sexagesimalWatcher);
        lngMinutes.addTextChangedListener(sexagesimalWatcher);
        lngMinutesFraction.addTextChangedListener(sexagesimalWatcher);

        sLatDegrees.addTextChangedListener(sexagesimalSecondsWatcher);
        sLatMinutes.addTextChangedListener(sexagesimalSecondsWatcher);
        sLatSeconds.addTextChangedListener(sexagesimalSecondsWatcher);
        sLngDegrees.addTextChangedListener(sexagesimalSecondsWatcher);
        sLngMinutes.addTextChangedListener(sexagesimalSecondsWatcher);
        sLngSeconds.addTextChangedListener(sexagesimalSecondsWatcher);

        dLatDegrees.addTextChangedListener(decimalWatcher);
        dLatDegreesFraction.addTextChangedListener(decimalWatcher);
        dLngDegrees.addTextChangedListener(decimalWatcher);
        dLngDegreesFraction.addTextChangedListener(decimalWatcher);

        etAzimuth.addTextChangedListener(azimuthWatcher);
        etDistance.addTextChangedListener(azimuthWatcher);
    }

    private void stopWatch() {
        latDegrees.removeTextChangedListener(sexagesimalWatcher);
        latMinutes.removeTextChangedListener(sexagesimalWatcher);
        latMinutesFraction.removeTextChangedListener(sexagesimalWatcher);
        lngDegrees.removeTextChangedListener(sexagesimalWatcher);
        lngMinutes.removeTextChangedListener(sexagesimalWatcher);
        lngMinutesFraction.removeTextChangedListener(sexagesimalWatcher);

        sLatDegrees.removeTextChangedListener(sexagesimalSecondsWatcher);
        sLatMinutes.removeTextChangedListener(sexagesimalSecondsWatcher);
        sLatSeconds.removeTextChangedListener(sexagesimalSecondsWatcher);
        sLngDegrees.removeTextChangedListener(sexagesimalSecondsWatcher);
        sLngMinutes.removeTextChangedListener(sexagesimalSecondsWatcher);
        sLngSeconds.removeTextChangedListener(sexagesimalSecondsWatcher);

        dLatDegrees.removeTextChangedListener(decimalWatcher);
        dLatDegreesFraction.removeTextChangedListener(decimalWatcher);
        dLngDegrees.removeTextChangedListener(decimalWatcher);
        dLngDegreesFraction.removeTextChangedListener(decimalWatcher);

        etAzimuth.removeTextChangedListener(azimuthWatcher);
        etDistance.removeTextChangedListener(azimuthWatcher);
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActivity(this);
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

    private void onSaveCheckpoint() {
        try {
            checkpointManager.addCheckpoint(geoCache.getId(), name.getText().toString(), currentInputGeoPoint.getLatitudeE6(), currentInputGeoPoint.getLongitudeE6());
            finish();
        } catch (Exception e) {
            LogManager.e(TAG, e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
        }
    }

    class SexagesimalListener extends TextChangeListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int degreesInt = Integer.parseInt(latDegrees.getText().toString());
                int minutesInt = Integer.parseInt(latMinutes.getText().toString());
                float minutesFloat = Float.parseFloat("." + latMinutesFraction.getText());
                int latitudeE6 = new Sexagesimal(degreesInt, (double)minutesInt + minutesFloat).toCoordinateE6();

                degreesInt = Integer.parseInt(lngDegrees.getText().toString());
                minutesInt = Integer.parseInt(lngMinutes.getText().toString());
                minutesFloat = Float.parseFloat("." + lngMinutesFraction.getText());
                int longitudeE6 = new Sexagesimal(degreesInt, (double)minutesInt + minutesFloat).toCoordinateE6();

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

    class SexagesimalSecondsListener extends TextChangeListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int degreesInt = Integer.parseInt(sLatDegrees.getText().toString());
                int minutesInt = Integer.parseInt(sLatMinutes.getText().toString());
                // http://code.google.com/p/android-geocaching/issues/detail?id=232
                String seconds = sLatSeconds.getText().toString().replace('.', secFormat.getDecimalFormatSymbols().getDecimalSeparator());
                float secondsFloat = secFormat.parse(seconds).floatValue();
                int latitudeE6 = new SexagesimalSec(degreesInt, minutesInt, secondsFloat).toCoordinateE6();

                degreesInt = Integer.parseInt(sLngDegrees.getText().toString());
                minutesInt = Integer.parseInt(sLngMinutes.getText().toString());
                // http://code.google.com/p/android-geocaching/issues/detail?id=232
                seconds = sLngSeconds.getText().toString().replace('.', secFormat.getDecimalFormatSymbols().getDecimalSeparator());
                secondsFloat = secFormat.parse(seconds).floatValue();
                int longitudeE6 = new SexagesimalSec(degreesInt, minutesInt, secondsFloat).toCoordinateE6();

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

    class DecimalListener extends TextChangeListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                double latitudeE6 = (double)(Integer.parseInt(dLatDegrees.getText().toString()) + Float.parseFloat("." + dLatDegreesFraction.getText())) * 1E6;
                double longitudeE6 = (double)(Integer.parseInt(dLngDegrees.getText().toString()) + Float.parseFloat("." + dLngDegreesFraction.getText())) * 1E6;
                currentInputGeoPoint = new GeoPoint((int)latitudeE6, (int)longitudeE6);
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

    class AzimuthListener extends TextChangeListener {

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

    class TextChangeListener implements TextWatcher {

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
