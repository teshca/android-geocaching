package su.geocaching.android.ui.searchmap;

import android.location.Location;
import su.geocaching.android.controller.compass.ICompassView;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.map.IMapWrapper;

public interface ISearchMapWrapper extends IMapWrapper, ICompassView {
    void updateLocationMarker(Location location, boolean isLocationPrecise);
    void setLocationPrecise(boolean isLocationPrecise);

    void setSearchGeocache(GeoCache geoCache);
    void clearGeocacheMarkers();
}