package su.geocaching.android.ui.checkpoints;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import android.widget.TextView.BufferType;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.compass.CompassHelper;
import su.geocaching.android.controller.managers.AccurateUserLocationManager;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.controller.utils.Sexagesimal;
import su.geocaching.android.controller.utils.SexagesimalSec;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoPoint;
import su.geocaching.android.ui.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Coordinate converter for waypoints
 *
 * @author Nikita Bumakov
 */
public class CreateCheckpointActivity extends SherlockActivity {

    private static final String TAG = CreateCheckpointActivity.class.getCanonicalName();
    private static final String CREATE_CHECK_POINT_ACTIVITY_NAME = "/CreateCheckpointActivity";

    private Handler mHandler = new Handler();

    private ScrollView checkpointScrollView;
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

    private static final DecimalFormat degreesFractionFormat = new DecimalFormat("000000");
    private static final DecimalFormat minutesFractionFormat = new DecimalFormat("000");
    private static final DecimalFormat minFormat = new DecimalFormat("00");
    private static final DecimalFormat secFormat = new DecimalFormat("00.00");

    private static final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // We will use US separator because of this bug ın Androıd
        // http://code.google.com/p/android-geocaching/issues/detail?id=232
        secFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_checkpoint_activity);

        geoCache = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());
        checkpointManager = Controller.getInstance().getCheckpointManager(geoCache.getId());

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(geoCache.getName());

        checkpointScrollView = (ScrollView) findViewById(R.id.checkpointScrollView);

        sexagesimal = (LinearLayout) findViewById(R.id.SexagesimalLayout);
        sexagesimalSeconds = (LinearLayout) findViewById(R.id.SexagesimalSeconsdLayout);
        decimal = (LinearLayout) findViewById(R.id.DecimalLayout);
        azimuth = (LinearLayout) findViewById(R.id.AzimuthLayout);

        sexagesimalWatcher = new SexagesimalListener();
        sexagesimalSecondsWatcher = new SexagesimalSecondsListener();
        decimalWatcher = new DecimalListener();
        azimuthWatcher = new AzimuthListener();

        ToggleButton sexagesimalToggle = (ToggleButton) findViewById(R.id.sexagestimal_toggle_button);
        sexagesimalToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibility(sexagesimal, isChecked);
            }
        });

        ToggleButton sexagesimalSecondsToggle = (ToggleButton) findViewById(R.id.sexagestimal_seconds_toggle_button);
        sexagesimalSecondsToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibility(sexagesimalSeconds, isChecked);
            }
        });

        ToggleButton decimalToggle = (ToggleButton) findViewById(R.id.decimal_toggle_button);
        decimalToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibility(decimal, isChecked);
            }
        });

        ToggleButton azimuthToggle = (ToggleButton) findViewById(R.id.azimuth_toggle_button);
        azimuthToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibility(azimuth, isChecked);
            }
        });

        initInputFields();

        updateTextBoxes();

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(CREATE_CHECK_POINT_ACTIVITY_NAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        startWatch();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopWatch();
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

    private void initInputFields() {

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
            currentLocation = geoCache.getGeoPoint();
            info.setText(R.string.relative_to_cache_location);
        }

    }

    private void updateTextBoxes() {
        if (currentInputGeoPoint == null) {
            currentInputGeoPoint = geoCache.getGeoPoint();
        }
        updateSexagesimal();
        updateSexagesimalSeconds();
        updateDecimal();
        updateAzimuth();
    }

    private void updateSexagesimal() {
        double lat = currentInputGeoPoint.getLatitude();
        double lng = currentInputGeoPoint.getLongitude();

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
        double lat = currentInputGeoPoint.getLatitude();
        double lng = currentInputGeoPoint.getLongitude();

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
        double lat = currentInputGeoPoint.getLatitude();
        double lng = currentInputGeoPoint.getLongitude();

        dLatDegrees.setText(Integer.toString((int)lat), BufferType.EDITABLE);
        dLngDegrees.setText(Integer.toString((int)lng), BufferType.EDITABLE);
        dLatDegreesFraction.setText(degreesFractionFormat.format(Math.abs(lat) % 1), BufferType.EDITABLE);
        dLngDegreesFraction.setText(degreesFractionFormat.format(Math.abs(lng) % 1), BufferType.EDITABLE);
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

    private void setVisibility(final View v, boolean isVisible) {
        if (isVisible) {
            v.setVisibility(View.VISIBLE);
            //checkpointScrollView.requestChildFocus(v, v);
            v.requestFocus();
            mHandler.post(
                    new Runnable() {
                        public void run() {
                            checkpointScrollView.requestChildRectangleOnScreen(v, new Rect(0, 0, v.getWidth(), v.getHeight()), false);
                        }
                    });
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private void onSaveCheckpoint() {
        try {
            checkpointManager.addCheckpoint(geoCache.getId(), name.getText().toString(), new GeoPoint(currentInputGeoPoint));
            finish();
        } catch (Exception e) {
            LogManager.e(TAG, e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
        }
    }

    private static String ERROR_MESSAGE =
            android.os.Build.VERSION.SDK_INT > 11 ?
                    Controller.getInstance().getResourceManager().getString(R.string.checkpoint_general_parsing_error) : "!";

    private static int ParseIntValue(TextView textView) {
        String text = textView.getText().toString();
        if (text.length() == 0) return 0;

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            textView.setError(ERROR_MESSAGE);
            throw e;
        }
    }

    private static float ParseFractionValue(TextView textView) {
        String text = textView.getText().toString();
        if (text.length() == 0) return 0;

        try {
            return Float.parseFloat("." + text);
        } catch (NumberFormatException e) {
            textView.setError(ERROR_MESSAGE);
            throw e;
        }
    }

    private static float ParseFloatValue(TextView textView) throws ParseException {
        String text = textView.getText().toString();
        if (text.length() == 0) return 0;

        try {
            return secFormat.parse(text).floatValue();
        } catch (ParseException e) {
            textView.setError(ERROR_MESSAGE);
            throw e;
        }
    }

    class SexagesimalListener extends TextChangeListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                // Latitude
                int degreesInt = ParseIntValue(latDegrees);
                int minutesInt = ParseIntValue(latMinutes);
                float minutesFloat = ParseFractionValue(latMinutesFraction);

                if (degreesInt > 80 || degreesInt < -80) {
                    latDegrees.setError(ERROR_MESSAGE);
                    return;
                }
                if (minutesInt > 59) {
                    latMinutes.setError(ERROR_MESSAGE);
                    return;
                }
                double latitude = new Sexagesimal(degreesInt, (double) minutesInt + minutesFloat).toCoordinate();

                // Longitude
                degreesInt = ParseIntValue(lngDegrees);
                minutesInt = ParseIntValue(lngMinutes);
                minutesFloat = ParseFractionValue(lngMinutesFraction);
                if (degreesInt > 180 || degreesInt < -180) {
                    lngDegrees.setError(ERROR_MESSAGE);
                    return;
                }
                if (minutesInt > 59) {
                    lngMinutes.setError(ERROR_MESSAGE);
                    return;
                }
                double longitude = new Sexagesimal(degreesInt, (double) minutesInt + minutesFloat).toCoordinate();

                currentInputGeoPoint = new GeoPoint(latitude, longitude);
                stopWatch();
                updateSexagesimalSeconds();
                updateDecimal();
                updateAzimuth();
                startWatch();
            } catch (NumberFormatException e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
    }

    class SexagesimalSecondsListener extends TextChangeListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                // Latitude
                int degreesInt = ParseIntValue(sLatDegrees);
                int minutesInt = ParseIntValue(sLatMinutes);
                float secondsFloat = ParseFloatValue(sLatSeconds);
                if (degreesInt > 80 || degreesInt < -80) {
                    dLatDegrees.setError(ERROR_MESSAGE);
                    return;
                }
                if (minutesInt > 59) {
                    sLatMinutes.setError(ERROR_MESSAGE);
                    return;
                }
                if (secondsFloat >= 60) {
                    sLatSeconds.setError(ERROR_MESSAGE);
                    return;
                }
                double latitude = new SexagesimalSec(degreesInt, minutesInt, secondsFloat).toCoordinate();

                // Longitude
                degreesInt = ParseIntValue(sLngDegrees);
                minutesInt = ParseIntValue(sLngMinutes);
                secondsFloat = ParseFloatValue(sLngSeconds);
                if (degreesInt > 180 || degreesInt < -180) {
                    sLngDegrees.setError(ERROR_MESSAGE);
                    return;
                }
                if (minutesInt > 59) {
                    sLngMinutes.setError(ERROR_MESSAGE);
                    return;
                }
                if (secondsFloat >= 60) {
                    sLngSeconds.setError(ERROR_MESSAGE);
                    return;
                }
                double longitude = new SexagesimalSec(degreesInt, minutesInt, secondsFloat).toCoordinate();

                currentInputGeoPoint = new GeoPoint(latitude, longitude);
                stopWatch();
                updateSexagesimal();
                updateDecimal();
                updateAzimuth();
                startWatch();
            } catch (NumberFormatException e) {
                LogManager.e(TAG, e.getMessage(), e);
            } catch (ParseException e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
    }

    class DecimalListener extends TextChangeListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                double latitude = ParseIntValue(dLatDegrees) + ParseFractionValue(dLatDegreesFraction);
                double longitude = ParseIntValue(dLngDegrees) + ParseFractionValue(dLngDegreesFraction);

                if (latitude > 80 || latitude < -80) {
                    dLatDegrees.setError(ERROR_MESSAGE);
                    return;
                }
                if (longitude > 180 || longitude < -180) {
                    dLngDegrees.setError(ERROR_MESSAGE);
                    return;
                }

                currentInputGeoPoint = new GeoPoint(latitude, longitude);
                stopWatch();
                updateSexagesimal();
                updateSexagesimalSeconds();
                updateAzimuth();
                startWatch();
            } catch (NumberFormatException e) {
                LogManager.e(TAG, e.getMessage(), e);
            }
        }
    }

    class AzimuthListener extends TextChangeListener {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int azimuth = ParseIntValue(etAzimuth);
                if (azimuth > 360) {
                    etAzimuth.setError(ERROR_MESSAGE);
                    return;
                }
                int distance = ParseIntValue(etDistance);
                currentInputGeoPoint = CoordinateHelper.distanceBearingToGeoPoint(currentLocation, azimuth, distance);
                stopWatch();
                updateDecimal();
                updateSexagesimal();
                updateSexagesimalSeconds();
                startWatch();
            } catch (NumberFormatException e) {
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
