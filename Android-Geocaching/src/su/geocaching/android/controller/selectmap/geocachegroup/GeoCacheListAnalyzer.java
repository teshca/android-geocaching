package su.geocaching.android.controller.selectmap.geocachegroup;

import android.graphics.Point;
import android.os.AsyncTask;
import com.google.android.maps.MapView;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison
 * @since 17.02.11
 */

public class GeoCacheListAnalyzer {
    private MapView map;

    private static final int MINIMUM_GROUP_SIZE_TO_CREATE_CLUSTER = 2;
    private static final int FINGER_SIZE_X = 60;
    private static final int FINGER_SIZE_Y = 80;

    public GeoCacheListAnalyzer(MapView map) {
        this.map = map;
    }

    private LinkedList<GeoCacheOverlayItem> createOverlayItemList(List<Centroid> centroidList) {
        final LinkedList<GeoCacheOverlayItem> overlayItemList = new LinkedList<GeoCacheOverlayItem>();
        for (Centroid centroid : centroidList) {
            int num = centroid.getNumberOfView();
            if (num != 0) {
                if (num < MINIMUM_GROUP_SIZE_TO_CREATE_CLUSTER) {
                    overlayItemList.add(new GeoCacheOverlayItem(centroid.getCache(), "", ""));
                } else {
                    overlayItemList.add(new GeoCacheOverlayItem(map.getProjection().fromPixels(centroid.getX(), centroid.getY()), "Group", ""));
                }
            }
        }
        return overlayItemList;
    }

    private List<Centroid> generateCentroids() {
        final int sizeX = map.getWidth() / FINGER_SIZE_X;
        final int sizeY = map.getHeight() / FINGER_SIZE_Y;
        List<Centroid> centroids = new LinkedList<Centroid>();

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                centroids.add(new Centroid((int) ((i + 0.5) * FINGER_SIZE_X), (int) ((j + 0.5) * FINGER_SIZE_Y), null));
            }
        }
        return centroids;
    }

    public List<GeoCacheOverlayItem> getGroupedList(List<GeoCache> geoCacheList, AsyncTask<?,?,?> asyncTask) {
        final List<Centroid> centroids = generateCentroids();
        if (asyncTask.isCancelled()) return null;
        final List<GeoCacheView> points = generatePointsList(geoCacheList);
        if (asyncTask.isCancelled()) return null;
        final List<Centroid> centroidList = new KMeans(points, centroids, asyncTask).getCentroids();
        if (asyncTask.isCancelled()) return null;
        return createOverlayItemList(centroidList);
    }

    private List<GeoCacheView> generatePointsList(List<GeoCache> geoCacheList) {
        List<GeoCacheView> list = new LinkedList<GeoCacheView>();
        for (GeoCache cache : geoCacheList) {
            Point point = map.getProjection().toPixels(cache.getLocationGeoPoint(), null);
            list.add(new GeoCacheView(point.x, point.y, cache));
        }
        return list;
    }
}
