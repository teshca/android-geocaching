package su.geocaching.android.ui.searchmap.stepbystep;

import su.geocaching.android.controller.CheckpointManager;
import su.geocaching.android.controller.Controller;
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

public class DecimalInputActivity extends Activity {
  
    private static final String TAG = DecimalInputActivity.class.getCanonicalName();
    private static final String DECIMAL_INPUT_ACTIVITY_FOLDER = "/DecimalInputActivity";
    private EditText latDegrees, latFraction;
    private EditText lngDegrees, lngFraction;
    private CheckpointManager checkpointManager;
    private TextWatcher textWacher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decimal_input);

        latDegrees = (EditText) findViewById(R.id.dLatDegrees);
        latFraction = (EditText) findViewById(R.id.dLatFraction);
        lngDegrees = (EditText) findViewById(R.id.dLngDegrees);
        lngFraction = (EditText) findViewById(R.id.dLngFraction);

        textWacher = new TextChangeListener();
        Controller.getInstance().getGoogleAnalyticsManager(this).trackPageView(DECIMAL_INPUT_ACTIVITY_FOLDER);
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

        latDegrees.setText(Integer.toString(lat / 1000000), BufferType.EDITABLE);
        lngDegrees.setText(Integer.toString(lng / 1000000), BufferType.EDITABLE);
        latFraction.setText(Integer.toString(lat % 1000000), BufferType.EDITABLE);
        lngFraction.setText(Integer.toString(lng % 1000000), BufferType.EDITABLE);

        latDegrees.addTextChangedListener(textWacher);
        latFraction.addTextChangedListener(textWacher);
        lngDegrees.addTextChangedListener(textWacher);
        lngFraction.addTextChangedListener(textWacher);

        super.onResume();
    }
    
    @Override
    protected void onPause() {
        latDegrees.removeTextChangedListener(textWacher);
        latFraction.removeTextChangedListener(textWacher);
        lngDegrees.removeTextChangedListener(textWacher);
        lngFraction.removeTextChangedListener(textWacher);
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
            int latitudeE6 = Integer.parseInt(latDegrees.getText().toString()) * 1000000 + Integer.parseInt(latFraction.getText().toString());
            int longitudeE6 = Integer.parseInt(lngDegrees.getText().toString()) * 1000000 + Integer.parseInt(lngFraction.getText().toString());
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
                int latitudeE6 = Integer.parseInt(latDegrees.getText().toString()) * 1000000 + Integer.parseInt(latFraction.getText().toString());
                int longitudeE6 = Integer.parseInt(lngDegrees.getText().toString()) * 1000000 + Integer.parseInt(lngFraction.getText().toString());
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
