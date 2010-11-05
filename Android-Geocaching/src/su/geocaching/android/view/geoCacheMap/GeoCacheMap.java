package su.geocaching.android.view.geoCacheMap;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import com.google.android.maps.*;
import su.geocaching.android.view.R;

import java.util.List;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        Search GeoCache with the map.
 */
public abstract class GeoCacheMap extends MapActivity {
    protected MapView mvMap;
    protected MapController mcMapController;
    protected UserLocationOverlay userOverlay;
    protected SearchGeoCacheCompassManager compassManager;
    protected SearchGeoCacheLocationManager locationManager;
    protected List<Overlay> mapOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        // int layout = intent.getIntExtra("layout", R.layout.select_geocache_map);
        // int id = intent.getIntExtra("id", R.id.selectGeocacheMap);

        setContentView(R.layout.select_geocache_map);
        mvMap = (MapView) findViewById(R.id.selectGeocacheMap);
        //TODO: deal with the transfer of resources
        // Log.d("albama", Integer.toString(id));
        // Log.d("albama", Boolean.toString(mvMap == null));
        mcMapController = mvMap.getController();
        mvMap.setBuiltInZoomControls(true);
        mapOverlays = mvMap.getOverlays();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassManager = new SearchGeoCacheCompassManager(this);
        locationManager = new SearchGeoCacheLocationManager(this);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public void updateUserOverlay(Location location, float angle) {
        GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6),
                (int) (location.getLongitude() * 1E6));
        if (userOverlay == null) {
            userOverlay = new UserLocationOverlay(this, point, angle, location.getAccuracy());
        } else {
            mapOverlays.remove(userOverlay);
            userOverlay.setPoint(point);
            userOverlay.setAngle(angle);
            userOverlay.setRadius(location.getAccuracy());
        }
        mapOverlays.add(userOverlay);
        mvMap.invalidate();
    }

    public float getLastAzimuth() {
        return compassManager.getLastAzimuth();
    }

    public Location getLastLocation() {
        return locationManager.getCurrentLocation();
    }
}
