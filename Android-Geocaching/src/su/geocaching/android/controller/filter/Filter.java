package su.geocaching.android.controller.filter;

import su.geocaching.android.model.dataType.GeoCache;

import java.util.LinkedList;

/**
 * Author: Yuri Denison
 * Date: 05.11.2010 1:49:20
 */
public interface Filter {
    /**
     * @param list of GeoCaches
     * @return filtered input list by parameter
     */
    public LinkedList<GeoCache> filter(LinkedList<GeoCache> list);
}