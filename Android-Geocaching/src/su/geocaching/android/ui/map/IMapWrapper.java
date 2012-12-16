package su.geocaching.android.ui.map;

import android.location.Location;
import com.google.android.gms.maps.Projection;
import su.geocaching.android.model.MapInfo;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.List;

public interface IMapWrapper {

    void animateToLocation(Location location);

    MapInfo getMapState();
    void restoreMapSate(MapInfo lastMapInfo);

    void setZoomControlsEnabled(boolean zoomControlsEnabled);

    void setViewPortChangedListener(ViewPortChangedListener listener);

    Projection getProjection();

    void updateLocationMarker(Location location);

    void setupMyLocationLayer();

    void updateGeoCacheOverlay(List<GeoCacheOverlayItem> overlayItemList);

    void clearGeocacheOverlay();
}
