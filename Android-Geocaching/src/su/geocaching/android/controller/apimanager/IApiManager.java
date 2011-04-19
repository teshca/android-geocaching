package su.geocaching.android.controller.apimanager;

import com.google.android.maps.GeoPoint;

import su.geocaching.android.model.GeoCache;

import java.util.List;

/**
 * @author Nikita Bumakov
 */
public interface IApiManager {

    /**
     * Get list of GeoCache in search area
     *
     * @param upperLeftCorner  //TODO describe it
     * @param lowerRightCorner //TODO describe it
     * @return List of geoCash in visible area
     */
    public List<GeoCache> getGeoCacheList(GeoPoint upperLeftCorner, GeoPoint lowerRightCorner);
}