package su.geocaching.android.ui.map;

import android.location.Location;
import com.google.android.gms.maps.Projection;
import su.geocaching.android.model.MapInfo;

public interface IMapWrapper {

    void animateToLocation(Location location);

    MapInfo getMapState();
    void restoreMapSate(MapInfo lastMapInfo);

    void setZoomControlsEnabled(boolean zoomControlsEnabled);

    void setViewPortChangedListener(ViewPortChangedListener listener);

    Projection getProjection();

    void setupMyLocationLayer();
    void updateLocationMarker(Location location);
}