package su.geocaching.android.ui.searchmap;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.compass.CompassSourceType;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.map.GoogleMapWrapper;

public class SearchGoogleMapWrapper extends GoogleMapWrapper implements ISearchMapWrapper {

    private Polyline cacheDirection;

    private final int preciseColor;
    private final int notPreciseColor;
    private static final int DISTANCE_STROKE_WIDTH = 4;

    GoogleGeocacheOverlay geocacheOverlay;

    public SearchGoogleMapWrapper(GoogleMap map, final Context context) {
        super(map);
        preciseColor = Controller.getInstance().getResourceManager().getColor(R.color.user_location_arrow_color_precise);
        notPreciseColor = Controller.getInstance().getResourceManager().getColor(R.color.user_location_arrow_color_not_precise);
        geocacheOverlay = new GoogleGeocacheOverlay(map);

        map.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        GeoCache geoCache = geocacheOverlay.getCacheByMarkerId(marker.getId());
                        NavigationManager.startInfoActivity(context, geoCache);
                        return true;
                    }
                });
    }

    @Override
    public void updateLocationMarker(Location location, boolean isPrecise) {
        super.updateLocationMarker(location);

        int color = isPrecise ? preciseColor : notPreciseColor;
        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (cacheDirection == null) {
            PolylineOptions options = new PolylineOptions();
            LatLng cachePosition = getCacheLocation(Controller.getInstance().getCurrentSearchPoint());
            options.add(userPosition);
            options.add(cachePosition);
            options.color(color);
            options.width(DISTANCE_STROKE_WIDTH);
            cacheDirection = googleMap.addPolyline(options);
        } else {
            cacheDirection.getPoints().set(0, userPosition);
            cacheDirection.setColor(color);
        }
    }

    /**
     * Change behaviour of arrow if location precise or not
     *
     * @param isLocationPrecise true if user location precise
     * @see su.geocaching.android.controller.managers.AccurateUserLocationManager#hasPreciseLocation()
     */
    public void setLocationPrecise(boolean isLocationPrecise) {
        int color = isLocationPrecise ? preciseColor : notPreciseColor;
        cacheDirection.setColor(color);
    }

    @Override
    public boolean setDirection(float direction) {
        if (locationChangedListener != null) {
            currentUserLocation.setBearing(direction);
            locationChangedListener.onLocationChanged(currentUserLocation);
        }
        return true;
    }

    @Override
    public void setSourceType(CompassSourceType sourceType) {
    }

    @Override
    public void setDeclination(float declination) {
    }

    @Override
    public void clearGeocacheMarkers() {
        geocacheOverlay.clearGeocacheMarkers();
    }

    @Override
    public void setSearchGeocache(GeoCache geoCache) {
        geocacheOverlay.addGeoCacheMarker(geoCache);
    }
}