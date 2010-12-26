package su.geocaching.android.controller.filter;

import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;

/**
 * Author: Yuri Denison Date: 05.11.2010 1:49:20
 */
public interface IFilter {
    /**
     * @param list
     *            of GeoCaches
     * @return filtered input list by parameter
     */
    public List<GeoCache> filter(List<GeoCache> list);
}