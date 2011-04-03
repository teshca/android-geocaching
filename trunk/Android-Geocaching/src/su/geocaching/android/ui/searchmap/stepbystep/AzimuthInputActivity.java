package su.geocaching.android.ui.searchmap.stepbystep;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.GeoCacheLocationManager;
import su.geocaching.android.controller.GpsHelper;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class AzimuthInputActivity extends Activity {

    private EditText azimuth, distance;
    private GeoPoint currentLocation;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.azimut_input);

        TextView info = (TextView) findViewById(R.id.tvAzimuthInputInfo);
        azimuth = (EditText) findViewById(R.id.azimuth);
        distance = (EditText) findViewById(R.id.distance);

        GeoCacheLocationManager locationManager = Controller.getInstance().getLocationManager();

        if (locationManager.hasLocation()) {
            currentLocation = GpsHelper.locationToGeoPoint(locationManager.getLastKnownLocation());
            info.setText(R.string.relative_to_current_location);
        } else {
            GeoCache gc = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());
            currentLocation = gc.getLocationGeoPoint();
            info.setText(R.string.relative_to_cache_location);
        }
    }

    public void onEnterClick(View v) {
        int azimuth = Integer.parseInt(this.azimuth.getText().toString());
        if (azimuth > 360) {
            Toast.makeText(this, getString(R.string.error_stepbystep_input), Toast.LENGTH_SHORT).show();
            return;
        }
        int distance = Integer.parseInt(this.distance.getText().toString());
        GeoPoint goalGP = GpsHelper.distanceBearingToGeoPoint(currentLocation, azimuth, distance);
        Controller.getInstance().getCheckpointManager(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId())
                .addCheckpoint(goalGP.getLatitudeE6(), goalGP.getLongitudeE6());
        finish();
    }
}
