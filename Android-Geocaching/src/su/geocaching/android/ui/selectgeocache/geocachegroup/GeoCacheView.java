package su.geocaching.android.ui.selectgeocache.geocachegroup;

import android.graphics.Point;
import com.google.android.maps.Projection;
import su.geocaching.android.model.datatype.GeoCache;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class GeoCacheView {
    private int x;
    private int y;
    private GeoCache cache;
    private GeoCacheView closestCentroid;

    public GeoCacheView(int x, int y, GeoCache cache) {
        this.x = x;
        this.y = y;
        this.cache = cache;
        closestCentroid = null;
    }

    public boolean equals(GeoCacheView pair) {
        return x == pair.x && y == pair.y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public GeoCache getCache() {
        return cache;
    }

    public GeoCacheView getClosestCentroid() {
        return closestCentroid;
    }

    public void setClosestCentroid(GeoCacheView closestCentroid) {
        this.closestCentroid = closestCentroid;
    }
}