package su.geocaching.android.controller.filter;

import su.geocaching.android.model.dataType.GeoCache;

import java.util.LinkedList;

/**
 * Author: Yuri Denison
 * Date: 05.11.2010 3:01:35
 */
public class StatusFilter implements IFilter {
    private String param;

    public StatusFilter(String param) {
        this.param = param;
    }

    @Override
    public LinkedList<GeoCache> filter(LinkedList<GeoCache> list) {
        LinkedList<GeoCache> res = new LinkedList<GeoCache>();
        for (GeoCache geoCache : list) {
            if (geoCache.getParam().get("status").equals(param)) {
                res.add(geoCache);
            }
        }
        return res;
    }
}
