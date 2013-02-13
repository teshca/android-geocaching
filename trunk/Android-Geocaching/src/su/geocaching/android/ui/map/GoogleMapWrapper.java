package su.geocaching.android.ui.map;

import android.graphics.Bitmap;
import android.location.Location;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.GeoRect;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoPoint;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.map.providers.*;

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
        return new MapInfo(cameraPosition.target.latitude, cameraPosition.target.longitude, cameraPosition.zoom);
    }

    @Override
    public void restoreMapSate(MapInfo lastMapInfo) {
        LatLng center = new LatLng(lastMapInfo.getCenterX(), lastMapInfo.getCenterY());
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
        GeoPoint tl = new GeoPoint(viewPortBounds.northeast.latitude, viewPortBounds.southwest.longitude);
        GeoPoint br = new GeoPoint(viewPortBounds.southwest.latitude, viewPortBounds.northeast.longitude);
        return new GeoRect(tl, br);
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

    private TileOverlay customTileOverlay;
    private MapType currentMapType;

    @Override
    public void updateMapLayer() {
        MapType mapType = Controller.getInstance().getPreferencesManager().getMapType();
        if (mapType == currentMapType) return;
        currentMapType = mapType;

        if (customTileOverlay != null) customTileOverlay.remove();

        switch (mapType) {
            case GoogleNormal: googleMap.setMapType(MAP_TYPE_NORMAL); return;
            case GoogleSatellite: googleMap.setMapType(MAP_TYPE_SATELLITE); return;
            case GoogleTerrain: googleMap.setMapType(MAP_TYPE_TERRAIN); return;
            case GoogleHybrid: googleMap.setMapType(MAP_TYPE_HYBRID); return;
        }

        googleMap.setMapType(MAP_TYPE_NONE);// Don't display any google layer

        UrlTileProvider provider = getTileProvider(mapType);
        if (provider != null) {
            customTileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            customTileOverlay.setZIndex(-100);
        }
    }

    private UrlTileProvider getTileProvider(MapType mapType) {
        //TODO: yandex provider
        /**
            It's not possible to create yandex provider because they use different projection and it's impossible to use
            custom projection with current stater of google maps api.

            EPSG:3395 - WGS 84 / World Mercator  на сфероиде. Эта проекция используется такими сервисами как Космоснимки, Яндекс карты, Карты mail.ru (спутник) и др.
            EPSG:3857 - WGS 84 / Pseudo-Mercator (Spherical Mercator) на сфере. Эта проекция используется такими сервисами как Google, Virtualearth, Maps-For-Free, Wikimapia, OpenStreetMap, Роскосмос, Навител, Nokia и др.
        */
        switch (mapType) {
            case OsmMapnik:
                return new MapnikOsmUrlTileProvider();
            case OsmCylcle:
                return new OpenCycleMapOsmUrlTileProvider();
            case OsmMapQuest:
                return new MapQuestOsmUrlTileProvider();
        }
        return null;
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

    protected static LatLng getCacheLocation(GeoCache geoCache) {
        return new LatLng(geoCache.getGeoPoint().getLatitude(), geoCache.getGeoPoint().getLongitude());
    }

    protected static LatLng getUserLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}