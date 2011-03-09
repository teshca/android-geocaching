package su.geocaching.android.ui.searchgeocache.stepbystep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.GpsHelper;

public class AzimutInputActivity extends Activity {

    private EditText azimuth, distance;
    GeoPoint currentGeoPoint;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.azimut_input);

        azimuth = (EditText) findViewById(R.id.azimuth);
        distance = (EditText) findViewById(R.id.distance);

        GeoCache gc = getIntent().getExtras().getParcelable(GeoCache.class.getCanonicalName());
        currentGeoPoint = gc.getLocationGeoPoint();
    }

    public void onEnterClick(View v) {
        int azimuth = Integer.parseInt(this.azimuth.getText().toString());
        if (azimuth > 360) {
            // TODO
            return;
        }
        int distance = Integer.parseInt(this.distance.getText().toString());
        GeoPoint goalGP = GpsHelper.distanceBearingToGeoPoint(currentGeoPoint, azimuth, distance);
        Intent intent = new Intent();
        intent.putExtra(StepByStepTabActivity.LATITUDE, goalGP.getLatitudeE6());
        intent.putExtra(StepByStepTabActivity.LONGITUDE, goalGP.getLongitudeE6());
        getParent().setResult(RESULT_OK, intent);
        finish();
    }
}
