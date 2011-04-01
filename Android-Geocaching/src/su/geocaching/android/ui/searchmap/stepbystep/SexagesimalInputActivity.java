package su.geocaching.android.ui.searchmap.stepbystep;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GpsHelper;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class SexagesimalInputActivity extends Activity {

    private static final String TAG = SexagesimalInputActivity.class.getCanonicalName();
    private EditText latDegrees, latMinutes, latmMinutes;
    private EditText lngDegrees, lngMinutes, lngmMinutes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sexagestimal_input);

        latDegrees = (EditText) findViewById(R.id.sLatDegrees);
        latMinutes = (EditText) findViewById(R.id.sLatMinutes);
        latmMinutes = (EditText) findViewById(R.id.sLatmMinutes);
        lngDegrees = (EditText) findViewById(R.id.sLngDegrees);
        lngMinutes = (EditText) findViewById(R.id.sLngMinutes);
        lngmMinutes = (EditText) findViewById(R.id.sLngmMinutes);

        GeoCache gc = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());

        int lat = gc.getLocationGeoPoint().getLatitudeE6();
        int lng = gc.getLocationGeoPoint().getLongitudeE6();

        int[] sexagesimal = GpsHelper.coordinateE6ToSexagesimal(lat);
        latDegrees.setText(Integer.toString(sexagesimal[0]), BufferType.EDITABLE);
        latMinutes.setText(Integer.toString(sexagesimal[1]), BufferType.EDITABLE);
        latmMinutes.setText(Integer.toString(sexagesimal[2]), BufferType.EDITABLE);

        sexagesimal = GpsHelper.coordinateE6ToSexagesimal(lng);
        lngDegrees.setText(Integer.toString(sexagesimal[0]), BufferType.EDITABLE);
        lngMinutes.setText(Integer.toString(sexagesimal[1]), BufferType.EDITABLE);
        lngmMinutes.setText(Integer.toString(sexagesimal[2]), BufferType.EDITABLE);

    }


    public void onEnterClick(View v) {
        try {
            int latitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(latDegrees.getText().toString()), Integer.parseInt(latMinutes.getText().toString()),
                    Integer.parseInt(latmMinutes.getText().toString()));
            int longitudeE6 = GpsHelper.sexagesimalToCoordinateE6(Integer.parseInt(lngDegrees.getText().toString()), Integer.parseInt(lngMinutes.getText().toString()),
                    Integer.parseInt(lngmMinutes.getText().toString()));
            Controller.getInstance().getCheckpointManager(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId()).addCheckpoint(latitudeE6, longitudeE6);
        } catch (Exception e) {
            LogManager.e(TAG, e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
