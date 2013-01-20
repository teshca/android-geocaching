package su.geocaching.android.ui.map;

import android.graphics.Bitmap;
import android.location.Location;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.apimanager.GeoRect;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.MapInfo;

import static com.google.android.gms.maps.GoogleMap.*;

public class GoogleMapWrapper implements IMapWrapper {

    protected GoogleMap googleMap;

    protected Location currentUserLocation;
    protected LocationSource.OnLocationChangedListener locationChangedListener;

    private static final Bitmap clickableBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ALPHA_8);
    private Marker userPositionClickArea; // hack to make user position clickable
    private LocationMarkerTapListener locationMarkerTapListener;

    public GoogleMapWrapper(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);

        map.setOnMarkerClickListener(
            new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (marker.getId().equals(userPositionClickArea.getId())) {
                        if (locationMarkerTapListener != null)
                            locationMarkerTapListener.OnMarkerTapped();
                        return true;
                    }
                    return onMarkerTap(marker);
                }
            });
    }

    protected boolean onMarkerTap(Marker marker) {
        return false;
    }

    @Override
    public void animateToLocation(Location location) {
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(center));
    }

    @Override
    public MapInfo getMapState() {
        CameraPosition cameraPosition = googleMap.getCameraPosition();
        int latE6 = toE6(cameraPosition.target.latitude);
        int lonE6 = toE6(cameraPosition.target.longitude);
        int zoom = (int) cameraPosition.zoom;
        return  new MapInfo(latE6, lonE6, zoom);
    }

    @Override
    public void restoreMapSate(MapInfo lastMapInfo) {
        LatLng center = new LatLng(lastMapInfo.getCenterX() * 1E-6, lastMapInfo.getCenterY() * 1E-6);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center, lastMapInfo.getZoom());
        googleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void setZoomControlsEnabled(boolean zoomControlEnabled) {
        googleMap.getUiSettings().setZoomControlsEnabled(zoomControlEnabled);
    }

    @Override
    public void setViewPortChangeListener(final ViewPortChangeListener listener) {
        googleMap.setOnCameraChangeListener(
                new OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        GeoRect viewPort = getViewPortGeoRect();
                        listener.OnViewPortChanged(viewPort);
                    }
                }
        );
    }

    @Override
    public Projection getProjection() {
        return googleMap.getProjection();
    }

    private GeoRect getViewPortGeoRect() {
        LatLngBounds viewPortBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        GeoPoint tl = new GeoPoint(toE6(viewPortBounds.northeast.latitude), toE6(viewPortBounds.southwest.longitude));
        GeoPoint br = new GeoPoint(toE6(viewPortBounds.southwest.latitude), toE6(viewPortBounds.northeast.longitude));
        return new GeoRect(tl, br);
    }

    protected static int toE6(double coordinate) {
        return (int) (coordinate * 1E6);
    }

    protected static GeoPoint toGeoPoint(LatLng latLng) {
        return new GeoPoint(toE6(latLng.latitude), toE6(latLng.longitude));
    }

    @Override
    public void updateLocationMarker(Location location) {
        if (locationChangedListener != null) {
            currentUserLocation = location;
            locationChangedListener.onLocationChanged(location);
        }

        // Update clickable area
        LatLng userPosition = getUserLocation(location);
        if (userPositionClickArea == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(userPosition);
            markerOptions.anchor(0.5f, 0.4f); // strange google maps bug
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(clickableBitmap));
            userPositionClickArea = googleMap.addMarker(markerOptions);
        } else {
            userPositionClickArea.setPosition(userPosition);
        }
    }

    @Override
    public void setLocationMarkerTapListener(LocationMarkerTapListener listener) {
        locationMarkerTapListener = listener;
    }

    @Override
    public void setupMyLocationLayer() {
        googleMap.setMyLocationEnabled(true);
        googleMap.setLocationSource(new LocationSource() {

            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                locationChangedListener = onLocationChangedListener;
            }

            @Override
            public void deactivate() {
                locationChangedListener = null;
            }
        });
    }

    public static LatLng getCacheLocation(GeoCache geoCache) {
        return new LatLng(geoCache.getLocationGeoPoint().getLatitudeE6() * 1E-6, geoCache.getLocationGeoPoint().getLongitudeE6() * 1E-6);
    }

    public static LatLng getUserLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}