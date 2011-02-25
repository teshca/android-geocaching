package su.geocaching.android.ui.selectgeocache.geocachegroup;

import su.geocaching.android.model.datatype.GeoCache;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: Yuri Denison
 * @since: 25.02.11
 */

public class Centroid extends GeoCacheView {
    private List<GeoCacheView> geoCacheList;

    public Centroid(int x, int y, GeoCache cache) {
        super(x, y, cache);
        geoCacheList = new LinkedList<GeoCacheView>();
    }

    public List<GeoCacheView> getGeoCacheList() {
        return geoCacheList;
    }

    public void setGeoCacheList(List<GeoCacheView> geoCacheList) {
        this.geoCacheList = geoCacheList;
    }
}
