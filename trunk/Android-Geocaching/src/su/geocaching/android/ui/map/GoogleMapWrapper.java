package su.geocaching.android.ui.map;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.GeoRect;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.google.android.gms.maps.GoogleMap.*;

public class GoogleMapWrapper implements IMapWrapper {

    private GoogleMap mMap;
    private Marker userMarker;
    private Polygon userAccuracyPolygon;

    private LocationSource.OnLocationChangedListener locationChangedListener;

    private HashMap<String, GeoCache> markers = new HashMap<String, GeoCache>();
    private HashMap<Integer, Marker> geocacheMarkers = new HashMap<Integer, Marker>();
    private List<Marker> groupMarkers = new ArrayList<Marker>();

    public GoogleMapWrapper(GoogleMap map, final Context context) {
        mMap = map;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        mMap.setOnMarkerClickListener(
            new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    GeoCache geoCache = markers.get(marker.getId());
                    if (geoCache.getType() == GeoCacheType.GROUP) {
                        LatLng latLng = getCacheLocation(geoCache);
                        Point point = mMap.getProjection().toScreenLocation(latLng);
                        mMap.animateCamera(CameraUpdateFactory.zoomBy(1, point));
                    } else {
                        NavigationManager.startInfoActivity(context, geoCache);
                    }
                    return true;
                }
            });
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
    public void updateGeoCacheOverlay(List<GeoCacheOverlayItem> overlayItemList) {
        //TODO Optimize. Reuse existing group markers
        for (Marker marker : groupMarkers) {
            removeGeoCacheMarker(marker);
        }
        groupMarkers.clear();

        HashSet<Integer> cacheIds = new HashSet<Integer>();

        for (GeoCacheOverlayItem geoCacheOverlayItem : overlayItemList) {
            GeoCache geoCache = geoCacheOverlayItem.getGeoCache();
            if (geoCache.getType() == GeoCacheType.GROUP) {
                Marker marker = addGeoCacheMarker(geoCache);
                groupMarkers.add(marker);
            } else {
                if (!geocacheMarkers.containsKey(geoCache.getId())) {
                    Marker marker = addGeoCacheMarker(geoCache);
                    geocacheMarkers.put(geoCache.getId(), marker);
                }
                cacheIds.add(geoCache.getId());
            }
        }

        // remove retired cache markers
        for (Integer cacheId : geocacheMarkers.keySet().toArray(new Integer[geocacheMarkers.size()])) {
            if (!cacheIds.contains(cacheId)) {
                removeGeoCacheMarker(geocacheMarkers.get(cacheId));
                geocacheMarkers.remove(cacheId);
            }
        }
    }

    private Marker addGeoCacheMarker(GeoCache geoCache) {
        Marker marker = mMap.addMarker(getGeocacheMarkerOptions(geoCache));
        markers.put(marker.getId(), geoCache);
        return marker;
    }

    private void removeGeoCacheMarker(Marker marker) {
        markers.remove(marker.getId());
        marker.remove();
    }

    private MarkerOptions getGeocacheMarkerOptions(GeoCache geoCache) {
        LatLng latLng = getCacheLocation(geoCache);
        int iconId = Controller.getInstance().getResourceManager().getMarkerResId(geoCache.getType(), geoCache.getStatus());
        return
                new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(iconId));
    }

    private static LatLng getCacheLocation(GeoCache geoCache) {
        return new LatLng(geoCache.getLocationGeoPoint().getLatitudeE6() * 1E-6, geoCache.getLocationGeoPoint().getLongitudeE6() * 1E-6);
    }

    @Override
    public void clearGeocacheOverlay() {
        // delete geocache markers
        for (Marker marker : geocacheMarkers.values()) {
            removeGeoCacheMarker(marker);
        }
        geocacheMarkers.clear();

        // delete group markers
        for (Marker marker : groupMarkers) {
            removeGeoCacheMarker(marker);
        }
        groupMarkers.clear();
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