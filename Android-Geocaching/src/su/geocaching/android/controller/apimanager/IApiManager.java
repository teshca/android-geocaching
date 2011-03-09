package su.geocaching.android.controller.apimanager;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.model.datatype.GeoCache;

import java.util.List;

/**
 * @author Nikita Bumakov
 */
public interface IApiManager {

    /**
     * Get list of GeoCache in search area
     *
     * @return List of geoCash in visible area
     */
    public List<GeoCache> getGeoCacheList(GeoPoint upperLeftCorner, GeoPoint lowerRightCorner);
}