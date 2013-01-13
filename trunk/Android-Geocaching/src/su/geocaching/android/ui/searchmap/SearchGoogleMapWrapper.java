package su.geocaching.android.ui.searchmap;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.compass.CompassSourceType;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.map.GoogleMapWrapper;

import java.util.Arrays;

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

        //TODO: tong click --> set active item
        //TODO: click on user arrow --> run compass

        map.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        GeoCache geoCache = geocacheOverlay.getCacheByMarkerId(marker.getId());

                        Controller.getInstance().Vibrate();

                        if (geoCache.getType() == GeoCacheType.CHECKPOINT) {
                            NavigationManager.startCheckpointDialog(context, geoCache);
                        } else {
                            NavigationManager.startInfoActivity(context, geoCache);
                        }

                        return true;
                    }
                });

        map.setOnMapLongClickListener(
                new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        GeoCache geocache = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache();
                        GeoCache checkpoint = new GeoCache();
                        checkpoint.setType(GeoCacheType.CHECKPOINT);
                        checkpoint.setName(geocache.getName());
                        checkpoint.setId(geocache.getId());
                        checkpoint.setLocationGeoPoint(toGeoPoint(latLng));
                        NavigationManager.startCreateCheckpointActivity(context, checkpoint);
                    }
                }
        );
    }

    @Override
    public void updateLocationMarker(Location location, boolean isPrecise) {
        super.updateLocationMarker(location);

        int color = isPrecise ? preciseColor : notPreciseColor;
        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng cachePosition = getCacheLocation(Controller.getInstance().getCurrentSearchPoint());
        if (cacheDirection == null) {
            PolylineOptions options = new PolylineOptions();
            options.add(userPosition);
            options.add(cachePosition);
            options.color(color);
            options.width(DISTANCE_STROKE_WIDTH);
            cacheDirection = googleMap.addPolyline(options);
        } else {
            cacheDirection.setPoints(Arrays.asList(userPosition, cachePosition));
            cacheDirection.setColor(color);
        }
    }

    @Override
    public void updateCacheDirection() {
        if (cacheDirection == null) {
            LatLng cachePosition = getCacheLocation(Controller.getInstance().getCurrentSearchPoint());
            cacheDirection.getPoints().set(1, cachePosition);
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

    @Override
    public void addCheckpointMarker(GeoCache checkpoint) {
        geocacheOverlay.addGeoCacheMarker(checkpoint);
    }
}