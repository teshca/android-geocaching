package su.geocaching.android.ui.searchmap.stepbystep;

import su.geocaching.android.controller.CheckpointManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsHelper;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class SexagestimalSecondsInputActivity extends Activity {

    private static final String TAG = SexagesimalInputActivity.class.getCanonicalName();
    private EditText latDegrees, latMinutes, latSeconds;
    private EditText lngDegrees, lngMinutes, lngSeconds;
    private CheckpointManager checkpointManager;
    private TextWatcher textWacher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sexagestimal_with_seconds);

        latDegrees = (EditText) findViewById(R.id.ssLatDegrees);
        latMinutes = (EditText) findViewById(R.id.ssLatMinutes);
        latSeconds = (EditText) findViewById(R.id.ssLatSeconds);
        lngDegrees = (EditText) findViewById(R.id.ssLngDegrees);
        lngMinutes = (EditText) findViewById(R.id.ssLngMinutes);
        lngSeconds = (EditText) findViewById(R.id.ssLngSeconds);

        textWacher = new TextChangeListener();
    }

    @Override
    protected void onResume() {
        GeoCache gc = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());
        checkpointManager = Controller.getInstance().getCheckpointManager(gc.getId());

        GeoPoint gp;
        if ((gp = checkpointManager.getLastInputGeoPoint()) == null) {
            gp = gc.getLocationGeoPoint();
        }

        int lat = gp.getLatitudeE6();
        int lng = gp.getLongitudeE6();

        float[] sexagesimal = GpsHelper.coordinateE6ToSecSexagesimal(lat);
        latDegrees.setText(Integer.toString((int) sexagesimal[0]), BufferType.EDITABLE);
        latMinutes.setText(Integer.toString((int) sexagesimal[1]), BufferType.EDITABLE);
        latSeconds.setText(Float.toString(sexagesimal[2]), BufferType.EDITABLE);

        sexagesimal = GpsHelper.coordinateE6ToSecSexagesimal(lng);
        lngDegrees.setText(Integer.toString((int) sexagesimal[0]), BufferType.EDITABLE);
        lngMinutes.setText(Integer.toString((int) sexagesimal[1]), BufferType.EDITABLE);
        lngSeconds.setText(Float.toString(sexagesimal[2]), BufferType.EDITABLE);

        latDegrees.addTextChangedListener(textWacher);
        latMinutes.addTextChangedListener(textWacher);
        latSeconds.addTextChangedListener(textWacher);
        lngDegrees.addTextChangedListener(textWacher);
        lngMinutes.addTextChangedListener(textWacher);
        lngSeconds.addTextChangedListener(textWacher);
        super.onResume();
    }

    @Override
    protected void onPause() {
        latDegrees.removeTextChangedListener(textWacher);
        latMinutes.removeTextChangedListener(textWacher);
        latSeconds.removeTextChangedListener(textWacher);
        lngDegrees.removeTextChangedListener(textWacher);
        lngMinutes.removeTextChangedListener(textWacher);
        lngSeconds.removeTextChangedListener(textWacher);
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
        try {
            int latitudeE6 = GpsHelper.secSexagesimalToCoordinateE6(Integer.parseInt(latDegrees.getText().toString()), Integer.parseInt(latMinutes.getText().toString()),
                    Float.parseFloat(latSeconds.getText().toString()));
            int longitudeE6 = GpsHelper.secSexagesimalToCoordinateE6(Integer.parseInt(lngDegrees.getText().toString()), Integer.parseInt(lngMinutes.getText().toString()),
                    Float.parseFloat(lngSeconds.getText().toString()));
            checkpointManager.addCheckpoint(latitudeE6, longitudeE6);
            checkpointManager.setLastInputGeoPoint(null);
            finish();
        } catch (Exception e) {
            LogManager.e(TAG, e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
        }

    }

    class TextChangeListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int latitudeE6 = GpsHelper.secSexagesimalToCoordinateE6(Integer.parseInt(latDegrees.getText().toString()), Integer.parseInt(latMinutes.getText().toString()),
                        Float.parseFloat(latSeconds.getText().toString()));
                int longitudeE6 = GpsHelper.secSexagesimalToCoordinateE6(Integer.parseInt(lngDegrees.getText().toString()), Integer.parseInt(lngMinutes.getText().toString()),
                        Float.parseFloat(lngSeconds.getText().toString()));
                checkpointManager.setLastInputGeoPoint(new GeoPoint(latitudeE6, longitudeE6));
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
