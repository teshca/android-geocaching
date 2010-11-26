package su.geocaching.android.controller.apimanager;

import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;

/**
 * @author Nikita Bumakov
 */
public interface IApiManager {

    /**
     * Get list of GeoCache in search area a
     * 
     *
     * @param maxLatitude
     *            - coordinate of visible area
     * @param minLatitude
     *            - coordinate of visible area
     * @param maxLongitude
     *            - coordinate of visible area
     * @param minLongitude
     *            - coordinate of visible area
     * @return List of geoCash in visible area
     */
    public List<GeoCache> getGeoCacheList(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude);
}