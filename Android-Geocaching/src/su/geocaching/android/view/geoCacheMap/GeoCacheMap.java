package su.geocaching.android.view.geoCacheMap;

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
    protected GeoCacheItemizedOverlay cacheItemizedOverlay;
    protected UserLocationOverlay userOverlay;
    protected SearchGeoCacheCompassManager compassManager;
    protected SearchGeoCacheLocationManager locationManager;
    protected List<Overlay> mapOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mvMap = (MapView) findViewById(R.id.searchGeocacheMap);
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
        //TODO: if (distanceToGeoCache<Accuracy) then startCompassView
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
