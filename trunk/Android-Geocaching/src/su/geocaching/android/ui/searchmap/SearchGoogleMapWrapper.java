package su.geocaching.android.ui.searchmap;

import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.compass.CompassSourceType;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.map.GeocacheMarkerTapListener;
import su.geocaching.android.ui.map.GoogleMapWrapper;
import su.geocaching.android.ui.map.MapLongClickListener;

import java.util.Arrays;
import java.util.Collection;

public class SearchGoogleMapWrapper extends GoogleMapWrapper implements ISearchMapWrapper {

    private Polyline cacheDirection;

    private final int preciseColor;
    private final int notPreciseColor;
    private static final int DISTANCE_STROKE_WIDTH = 4;

    private GoogleGeocacheOverlay geocacheOverlay;
    private GeocacheMarkerTapListener geocacheTapListener;
    private MapLongClickListener mapLongClickListener;

    private float currentDirection;

    private boolean autoRotationEnabled = false;
    private Handler uiThreadHandler = new Handler();

    public SearchGoogleMapWrapper(GoogleMap map) {
        super(map);
        preciseColor = Controller.getInstance().getResourceManager().getColor(R.color.user_location_arrow_color_precise);
        notPreciseColor = Controller.getInstance().getResourceManager().getColor(R.color.user_location_arrow_color_not_precise);
        geocacheOverlay = new GoogleGeocacheOverlay(map);

        uiThreadHandler = new Handler();

        map.setOnMapLongClickListener(
                new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        if (mapLongClickListener != null) {
                            mapLongClickListener.onMapLongClick(latLng);
                        }
                    }
                }
        );

        // hack to implement long click on cache markers
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                GeoCache geoCache = geocacheOverlay.getCacheByMarkerId(marker.getId());
                geocacheOverlay.removeGeoCacheMarker(marker);
                geocacheOverlay.addGeoCacheMarker(geoCache);
                if (geocacheTapListener != null) {
                    geocacheTapListener.OnMarkerLongTapped(geoCache);
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
        });
    }

    @Override
    protected boolean onMarkerTap(Marker marker) {
        GeoCache geoCache = geocacheOverlay.getCacheByMarkerId(marker.getId());
        if (geocacheTapListener != null && geoCache != null) {
            geocacheTapListener.OnMarkerTapped(geoCache);
            return true;
        }
        return false;
    }

    @Override
    public void updateLocationMarker(Location location, boolean isPrecise) {
        super.updateLocationMarker(location);

        int color = isPrecise ? preciseColor : notPreciseColor;
        LatLng userPosition = getUserLocation(location);
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
        if (cacheDirection != null) {
            LatLng cachePosition = getCacheLocation(Controller.getInstance().getCurrentSearchPoint());
            LatLng userPosition = getUserLocation(currentUserLocation);
            cacheDirection.setPoints(Arrays.asList(userPosition, cachePosition));
        }
    }

    @Override
    public void resetZoom(int width, int height, boolean animate) {
        Collection<GeoCache> geocaches = geocacheOverlay.getGeocaches();
        if (currentUserLocation == null && geocaches.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        // current user location
        if (currentUserLocation != null) {
            boundsBuilder.include(getUserLocation(currentUserLocation));
        }
        // geocache and checkpoint markers
        for (GeoCache geocache : geocaches) {
            boundsBuilder.include(getCacheLocation(geocache));
        }

        // TODO: take into account size of marker and it's anchor
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), width, height, 50);

        if (animate) {
            googleMap.animateCamera(cameraUpdate);
        } else {
            googleMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void setGeocacheTapListener(GeocacheMarkerTapListener listener) {
        geocacheTapListener = listener;
    }

    @Override
    public boolean isUserLocationMarkerCentered() {
        if (currentUserLocation == null) return false;
        Point userLocation = googleMap.getProjection().toScreenLocation(getUserLocation(currentUserLocation));
        Point mapCenter = googleMap.getProjection().toScreenLocation(googleMap.getCameraPosition().target);
        return Math.abs(userLocation.x - mapCenter.x) < 20 && Math.abs(userLocation.y - mapCenter.y) < 20;
    }

    @Override
    public boolean isAutoRotationEnabled() {
        return autoRotationEnabled;
    }

    @Override
    public void setAutoRotationEnabled(boolean b) {
        if (autoRotationEnabled == b) return;
        autoRotationEnabled = b;
        rotateMap(autoRotationEnabled ? currentDirection : 0, true);
    }

    @Override
    public void setMapLongClickListener(MapLongClickListener listener) {
        mapLongClickListener = listener;
    }

    /**
     * Change behaviour of arrow if location precise or not
     *
     * @param isLocationPrecise
     *         true if user location precise
     * @see su.geocaching.android.controller.managers.AccurateUserLocationManager#hasPreciseLocation()
     */
    public void setLocationPrecise(boolean isLocationPrecise) {
        if (cacheDirection != null) {
            final int color = isLocationPrecise ? preciseColor : notPreciseColor;
            cacheDirection.setColor(color);
        }
    }

    @Override
    public boolean setDirection(float direction) {
        currentDirection = direction;
        if (currentUserLocation != null) {
            currentUserLocation.setBearing(currentDirection);
            if (isAutoRotationEnabled()) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (isAutoRotationEnabled())
                            rotateMap(currentDirection, false);
                    }
                };
                uiThreadHandler.post(r);
            }
            if (locationChangedListener != null) {
                locationChangedListener.onLocationChanged(currentUserLocation);
            }
        }
        return true;
    }

    private boolean rotationAnimationInProgress = false;
    private synchronized void rotateMap(final float rotation, boolean animate) {
        CameraPosition currentCameraPosition = googleMap.getCameraPosition();
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(currentCameraPosition.target)
                        .bearing(rotation)
                        .zoom(currentCameraPosition.zoom)
                        .build();
        if (animate) {
            GoogleMap.CancelableCallback cancelableCallback = new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    rotationAnimationInProgress = false;
                }

                @Override
                public void onCancel() {
                    rotationAnimationInProgress = false;
                }
            };
            rotationAnimationInProgress = true;
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), cancelableCallback);
        } else {
            if (!rotationAnimationInProgress) {
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    @Override
    public void updateLocationMarker(Location location) {
        // bearing updated only with setDirection
        if (currentUserLocation != null) {
            location.setBearing(currentDirection);
        }
        super.updateLocationMarker(location);
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