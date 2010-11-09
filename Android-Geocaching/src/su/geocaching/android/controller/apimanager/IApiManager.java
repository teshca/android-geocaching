package su.geocaching.android.controller.apimanager;

import su.geocaching.android.model.datatype.GeoCache;

import java.util.LinkedList;

/**
 * @author Nikita Bumakov
 */
public interface IApiManager {

    /**
     * Get list of GeoCach in search area a
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
    public abstract LinkedList<GeoCache> getGeoCashList(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude);

    /**
     * @param id
     *            GeoCash id from geocaching.su
     * @return
     */
    public abstract GeoCache getGeoCacheByID(int id);

}