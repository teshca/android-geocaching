package su.geocaching.android.controller.filter;

import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;

/**
 * Author: Yuri Denison Date: 05.11.2010 1:50:54
 */
public class NoFilter implements IFilter {
    private static NoFilter instance;

    private NoFilter() {
    }

    public static NoFilter getInstance() {
	if (instance == null) {
	    synchronized (NoFilter.class) {
		if (instance == null) {
		    instance = new NoFilter();
		}
	    }
	}
	return instance;
    }

    @Override
    public List<GeoCache> filter(List<GeoCache> list) {
	return list;
    }
}
