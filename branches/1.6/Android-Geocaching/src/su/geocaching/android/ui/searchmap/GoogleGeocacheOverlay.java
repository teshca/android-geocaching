package su.geocaching.android.ui.searchmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.map.GoogleMarkerOptions;

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
        // it's draggable in order to implement long-click listener
        MarkerOptions draggableGeocacheMarkerOptions = GoogleMarkerOptions.fromGeocache(geoCache).draggable(true);
        Marker marker = googleMap.addMarker(draggableGeocacheMarkerOptions);
        geocaches.put(marker.getId(), geoCache);
        markers.add(marker);
        return marker;
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

    public void removeGeoCacheMarker(Marker marker) {
        geocaches.remove(marker.getId());
        marker.remove();
    }

    public Collection<GeoCache> getGeocaches() {
        return geocaches.values();
    }
}
