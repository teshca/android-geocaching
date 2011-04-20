package su.geocaching.android.controller.selectlogic.geocachegroup;

import su.geocaching.android.model.GeoCache;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class GeoCacheView {
    private int x;
    private int y;
    private GeoCache cache;
    private Centroid closestCentroid;

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

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public GeoCache getCache() {
        return cache;
    }

    public void setCache(GeoCache cache) {
        this.cache = cache;
    }

    public Centroid getClosestCentroid() {
        return closestCentroid;
    }

    public void setClosestCentroid(Centroid closestCentroid) {
        this.closestCentroid = closestCentroid;
    }
}
