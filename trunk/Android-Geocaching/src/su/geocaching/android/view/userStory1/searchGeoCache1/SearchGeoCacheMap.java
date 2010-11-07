package su.geocaching.android.view.userStory1.searchGeoCache1;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.dataType1.GeoCache;
import su.geocaching.android.view.MainMenu;
import su.geocaching.android.view.R;
import su.geocaching.android.view.geoCacheMap1.GeoCacheItemizedOverlay;
import su.geocaching.android.view.geoCacheMap1.GeoCacheMap;

/**
 * @author Android-Geocaching.su student project team
 * @description Search GeoCache with the map.
 * @since October 2010
 */
public class SearchGeoCacheMap extends GeoCacheMap {
    public final static String DEFAULT_GEOCACHE_ID_NAME = "GeoCache id";

    private GeoCache geoCache;
    private OverlayItem cacheOverlayItem;
    private GeoCacheItemizedOverlay cacheItemizedOverlay;
    private DistanceToGeoCacheOverlay distanceOverlay;
    private MyLocationOverlay userOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        Controller controller = Controller.getInstance();

        // not working yet
        // geoCache = controller.getGeoCacheByID(intent.getIntExtra(
        // MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
        geoCache = new GeoCache(intent.getIntExtra(
                MainMenu.DEFAULT_GEOCACHE_ID_NAME, -1));
        userOverlay = new MyLocationOverlay(this, map);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Drawable cacheMarker = this.getResources().getDrawable(
                R.drawable.orangecache);
        cacheMarker.setBounds(0, -cacheMarker.getMinimumHeight(),
                cacheMarker.getMinimumWidth(), 0);

        userOverlay.enableCompass();
        userOverlay.enableMyLocation();

        cacheItemizedOverlay = new GeoCacheItemizedOverlay(cacheMarker);
        cacheOverlayItem = new OverlayItem(geoCache.getLocationGeoPoint(), "",
                "");
        cacheItemizedOverlay.addOverlay(cacheOverlayItem);
        mapOverlays.add(cacheItemizedOverlay);
        mapOverlays.add(userOverlay);

        map.invalidate();
    }

    @Override
    protected void onPause() {
        super.onPause();

        userOverlay.disableCompass();
        userOverlay.disableMyLocation();
    }

    private void startCompassView() {
        Intent intent = new Intent(this, SearchGeoCacheCompass.class);
        intent.putExtra(DEFAULT_GEOCACHE_ID_NAME, geoCache.getId());
        startActivity(intent);
        this.finish();
    }

    protected float getDistanceToGeoCache(Location location) {
        float[] results = new float[3];
        double endLatitude = geoCache.getLocationGeoPoint().getLatitudeE6() / 1E6;
        double endLongitude = geoCache.getLocationGeoPoint().getLongitudeE6() / 1E6;
        Location.distanceBetween(location.getLatitude(),
                location.getLongitude(), endLatitude, endLongitude, results);
        return results[0];
    }

    @Override
    public void updateLocation(Location location) {
        Location loc = locationManager.getCurrentLocation();
        GeoPoint currentGeoPoint = new GeoPoint(
                (int) (loc.getLatitude() * 1E6),
                (int) (loc.getLongitude() * 1E6));

        if (distanceOverlay == null) { // It's really first run of update
            // location
            setDefaultZoom();
            distanceOverlay = new DistanceToGeoCacheOverlay(currentGeoPoint,
                    geoCache.getLocationGeoPoint());
            mapOverlays.add(distanceOverlay);
            return;
        }
        distanceOverlay.setCachePoint(geoCache.getLocationGeoPoint());
        distanceOverlay.setUserPoint(currentGeoPoint);
        map.invalidate();
    }

    public float getLastAzimuth() {
        return userOverlay.getOrientation();
    }

    @Override
    public Location getLastLocation() {
        return userOverlay.getLastFix();
    }

    private void setDefaultZoom() {
        Location loc = locationManager.getCurrentLocation();
        GeoPoint currentGeoPoint = new GeoPoint(
                (int) (loc.getLatitude() * 1E6),
                (int) (loc.getLongitude() * 1E6));
        mapController.zoomToSpan(
                Math.abs(geoCache.getLocationGeoPoint().getLatitudeE6()
                        - currentGeoPoint.getLatitudeE6()),
                Math.abs(geoCache.getLocationGeoPoint().getLongitudeE6()
                        - currentGeoPoint.getLongitudeE6()));

        GeoPoint center = new GeoPoint(
                (geoCache.getLocationGeoPoint().getLatitudeE6()
                        + currentGeoPoint.getLatitudeE6()) / 2,
                (geoCache.getLocationGeoPoint().getLongitudeE6()
                        + currentGeoPoint.getLongitudeE6()) / 2);
        mapController.animateTo(center);
    }
}