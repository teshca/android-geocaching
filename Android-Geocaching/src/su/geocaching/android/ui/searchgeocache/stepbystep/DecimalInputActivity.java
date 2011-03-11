package su.geocaching.android.ui.searchgeocache.stepbystep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;

public class DecimalInputActivity extends Activity {
    private static final String TAG = DecimalInputActivity.class.getCanonicalName();

    private EditText latDegrees, latFraction;
    private EditText lngDegrees, lngFraction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decimal_input);

        latDegrees = (EditText) findViewById(R.id.dLatDegrees);
        latFraction = (EditText) findViewById(R.id.dLatFraction);
        lngDegrees = (EditText) findViewById(R.id.dLngDegrees);
        lngFraction = (EditText) findViewById(R.id.dLngFraction);

        GeoCache gc = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());

        int lat = gc.getLocationGeoPoint().getLatitudeE6();
        int lng = gc.getLocationGeoPoint().getLongitudeE6();

        latDegrees.setText(Integer.toString(lat / 1000000), BufferType.EDITABLE);
        lngDegrees.setText(Integer.toString(lng / 1000000), BufferType.EDITABLE);

        latFraction.setText(Integer.toString(lat % 1000000), BufferType.EDITABLE);
        lngFraction.setText(Integer.toString(lng % 1000000), BufferType.EDITABLE);
    }

    public void onEnterClick(View v) {
        try {
            int latitudeE6 = Integer.parseInt(latDegrees.getText().toString()) * 1000000 + Integer.parseInt(latFraction.getText().toString());
            int longitudeE6 = Integer.parseInt(lngDegrees.getText().toString()) * 1000000 + Integer.parseInt(lngFraction.getText().toString());
            Intent intent = new Intent();
            intent.putExtra(StepByStepTabActivity.LATITUDE, latitudeE6);
            intent.putExtra(StepByStepTabActivity.LONGITUDE, longitudeE6);
            getParent().setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            LogManager.e(TAG, e.getMessage(), e);
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT);
        }
    }
}
