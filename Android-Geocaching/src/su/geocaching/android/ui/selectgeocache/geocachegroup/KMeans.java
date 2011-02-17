package su.geocaching.android.ui.selectgeocache.geocachegroup;

import java.util.HashMap;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class KMeans {
    private Pair[] points;
    private Pair[] centroids;
    private HashMap<Pair, Pair[]> resultMap;

    public KMeans(Pair[] points, Pair[] centroids) {
        this.points = points;
        this.centroids = centroids;
    }

    public HashMap<Pair, Pair[]> getClusterMap() {
        return resultMap;
    }
}
