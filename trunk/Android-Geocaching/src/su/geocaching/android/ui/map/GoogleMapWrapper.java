package su.geocaching.android.ui.map;

import android.location.Location;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.apimanager.GeoRect;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.R;

import static com.google.android.gms.maps.GoogleMap.*;

public class GoogleMapWrapper implements IMapWrapper {

    protected GoogleMap mMap;

    private Marker userMarker;
    private Polygon userAccuracyPolygon;

    private LocationSource.OnLocationChangedListener locationChangedListener;

    public GoogleMapWrapper(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    @Override
    public void animateToLocation(Location location) {
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(center));
    }

    @Override
    public MapInfo getMapState() {
        CameraPosition cameraPosition = mMap.getCameraPosition();
        int latE6 = (int) (cameraPosition.target.latitude * 1E6);
        int lonE6 = (int) (cameraPosition.target.longitude * 1E6);
        int zoom = (int) cameraPosition.zoom;
        return  new MapInfo(latE6, lonE6, zoom);
    }

    @Override
    public void restoreMapSate(MapInfo lastMapInfo) {
        LatLng center = new LatLng(lastMapInfo.getCenterX() * 1E-6, lastMapInfo.getCenterY() * 1E-6);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center, lastMapInfo.getZoom());
        mMap.moveCamera(cameraUpdate);
    }

    @Override
    public void setZoomControlsEnabled(boolean zoomControlEnabled) {
        mMap.getUiSettings().setZoomControlsEnabled(zoomControlEnabled);
    }

    @Override
    public void setViewPortChangedListener(final ViewPortChangedListener listener) {
        mMap.setOnCameraChangeListener(
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
        return mMap.getProjection();
    }

    private GeoRect getViewPortGeoRect() {
        LatLngBounds viewPortBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        GeoPoint tl = new GeoPoint(toE6(viewPortBounds.northeast.latitude), toE6(viewPortBounds.southwest.longitude));
        GeoPoint br = new GeoPoint(toE6(viewPortBounds.southwest.latitude), toE6(viewPortBounds.northeast.longitude));
        return new GeoRect(tl, br);
    }

    private static int toE6(double coordinate) {
        return (int) (coordinate * 1E6);
    }

    @Override
    public void updateLocationMarker(Location location) {
        if (locationChangedListener != null) {
            locationChangedListener.onLocationChanged(location);
        }

        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker ==  null) {
            userMarker = mMap.addMarker(
                    new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location))
                            .position(userPosition)
            );
        } else {
            userMarker.setPosition(userPosition);
        }

        //TODO: optimize. don't recreate if accuracy is not changed
        if (userAccuracyPolygon != null)
                userAccuracyPolygon.remove();

        userAccuracyPolygon = mMap.addPolygon(
                getCirclePolygonOption(userPosition, location.getAccuracy())
                        .strokeColor(ACCURACY_CIRCLE_STROKE_COLOR)
                        .strokeWidth(3)
                        .geodesic(true)
                        .fillColor(ACCURACY_CIRCLE_COLOR)
        );
    }

    @Override
    public void setupMyLocationLayer() {
        mMap.setMyLocationEnabled(true);
        mMap.setLocationSource(new LocationSource() {

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

    private static final int ACCURACY_CIRCLE_COLOR = 0x4000aa00;
    private static final int ACCURACY_CIRCLE_STROKE_COLOR = 0x40000000;

    private PolygonOptions getCirclePolygonOption(LatLng center, double radius) {
        PolygonOptions options = new PolygonOptions();

        double d2r = Math.PI / 180;

        double circleLat = (radius / 6378135) / d2r;
        double circleLng = circleLat / Math.cos(center.latitude * d2r);

        for (int i = 0; i <= 360; i++) {
            double theta = i * d2r;
            options.add(new LatLng(center.latitude + circleLat * Math.sin(theta),
                    center.longitude + circleLng * Math.cos(theta)));
        }

        return options;
    }
}