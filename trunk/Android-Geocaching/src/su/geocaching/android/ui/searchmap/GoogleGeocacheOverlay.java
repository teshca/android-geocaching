package su.geocaching.android.ui.searchmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.map.GoogleMapWrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class GoogleGeocacheOverlay {

    private GoogleMap googleMap;

    // markerId <---> geocache
    private HashMap<String, GeoCache> geocaches = new HashMap<String, GeoCache>();
    private LinkedList<Marker> markers = new LinkedList<Marker>();

    public GoogleGeocacheOverlay(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public Marker addGeoCacheMarker(GeoCache geoCache) {
        Marker marker = googleMap.addMarker(getGeocacheMarkerOptions(geoCache));
        geocaches.put(marker.getId(), geoCache);
        markers.add(marker);
        return marker;
    }

    private MarkerOptions getGeocacheMarkerOptions(GeoCache geoCache) {
        LatLng latLng = GoogleMapWrapper.getCacheLocation(geoCache);
        int iconId = Controller.getInstance().getResourceManager().getMarkerResId(geoCache.getType(), geoCache.getStatus());
        return
                new MarkerOptions()
                        .position(latLng)
                        .anchor(0.5f, 0.95f)
                        .icon(BitmapDescriptorFactory.fromResource(iconId));
    }

    public GeoCache getCacheByMarkerId(String markerId) {
        return geocaches.get(markerId);
    }

    public void clearGeocacheMarkers() {
        // delete geocache geocaches
        for (Marker marker : markers) {
            removeGeoCacheMarker(marker);
        }
        markers.clear();
    }

    private void removeGeoCacheMarker(Marker marker) {
        geocaches.remove(marker.getId());
        marker.remove();
    }

    public Collection<GeoCache> getGeocaches() {
        return geocaches.values();
    }
}
