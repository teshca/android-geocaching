package su.geocaching.android.ui.map;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;

public class GoogleMarkerOptions {
    public static MarkerOptions fromGeocache(GeoCache geoCache) {
        LatLng latLng = GoogleMapWrapper.getCacheLocation(geoCache);
        int iconId = Controller.getInstance().getResourceManager().getMarkerResId(geoCache.getType(), geoCache.getStatus());
        return
                new MarkerOptions()
                        .position(latLng)
                        .anchor(0.5f, 0.95f)
                        .icon(BitmapDescriptorFactory.fromResource(iconId));
    }
}
