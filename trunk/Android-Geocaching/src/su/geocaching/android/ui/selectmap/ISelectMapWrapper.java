package su.geocaching.android.ui.selectmap;

import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.map.IMapWrapper;

import java.util.List;

public interface ISelectMapWrapper extends IMapWrapper {
    void updateGeoCacheMarkers(List<GeoCache> geoCaches);
    void clearGeocacheMarkers();
}
