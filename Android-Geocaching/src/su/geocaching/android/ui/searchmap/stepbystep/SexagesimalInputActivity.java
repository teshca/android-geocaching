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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class SexagesimalInputActivity extends Activity {

    private static final String TAG = SexagesimalInputActivity.class.getCanonicalName();
    private EditText latDegrees, latMinutes, latmMinutes;
    private EditText lngDegrees, lngMinutes, lngmMinutes;
    private CheckpointManager checkpointManager;
    private TextWatcher textWacher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sexagestimal_input);

        latDegrees = (EditText) findViewById(R.id.sLatDegrees);
        latMinutes = (EditText) findViewById(R.id.sLatMinutes);
        latmMinutes = (EditText) findViewById(R.id.sLatmMinutes);
        lngDegrees = (EditText) findViewById(R.id.sLngDegrees);
        lngMinutes = (EditText) findViewById(R.id.sLngMinutes);
        lngmMinutes = (EditText) findViewById(R.id.sLngmMinutes);

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

        int[] sexagesimal = GpsHelper.coordinateE6ToSexagesimal(lat);
        latDegrees.setText(Integer.toString(sexagesimal[0]), BufferType.EDITABLE);
        latMinutes.setText(Integer.toString(sexagesimal[1]), BufferType.EDITABLE);
        latmMinutes.setText(Integer.toString(sexagesimal[2]), BufferType.EDITABLE);

        sexagesimal = GpsHelper.coordinateE6ToSexagesimal(lng);
        lngDegrees.setText(Integer.toString(sexagesimal[0]), BufferType.EDITABLE);
        lngMinutes.setText(Integer.toString(sexagesimal[1]), BufferType.EDITABLE);
        lngmMinutes.setText(Integer.toString(sexagesimal[2]), BufferType.EDITABLE);

        latDegrees.addTextChangedListener(textWacher);
        latMinutes.addTextChangedListener(textWacher);
        latmMinutes.addTextChangedListener(textWacher);
        lngDegrees.addTextChangedListener(textWacher);
        lngMinutes.addTextChangedListener(textWacher);
        lngmMinutes.addTextChangedListener(textWacher);
        super.onResume();
    }

    public void onEnterClick(View v) {
        try {
            int latitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(latDegrees.getText().toString()), Integer.parseInt(latMinutes.getText().toString()),
                    Integer.parseInt(latmMinutes.getText().toString()));
            int longitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(lngDegrees.getText().toString()), Integer.parseInt(lngMinutes.getText().toString()),
                    Integer.parseInt(lngmMinutes.getText().toString()));
            checkpointManager.addCheckpoint(latitudeE6, longitudeE6);
        } catch (Exception e) {
            LogManager.e(TAG, e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    class TextChangeListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int latitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(latDegrees.getText().toString()), Integer.parseInt(latMinutes.getText().toString()),
                        Integer.parseInt(latmMinutes.getText().toString()));
                int longitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(lngDegrees.getText().toString()), Integer.parseInt(lngMinutes.getText().toString()),
                        Integer.parseInt(lngmMinutes.getText().toString()));
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
