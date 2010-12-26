package su.geocaching.android.controller.filter;

import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;

/**
 * Author: Yuri Denison Date: 05.11.2010 3:01:35
 */
public class StatusFilter implements IFilter {
    private GeoCacheStatus param;

    public StatusFilter(GeoCacheStatus param) {
	this.param = param;
    }

    @Override
    public List<GeoCache> filter(List<GeoCache> list) {
	List<GeoCache> res = new LinkedList<GeoCache>();
	for (GeoCache geoCache : list) {
	    if (geoCache.getStatus() == param) {
		res.add(geoCache);
	    }
	}
	return res;
    }
}
