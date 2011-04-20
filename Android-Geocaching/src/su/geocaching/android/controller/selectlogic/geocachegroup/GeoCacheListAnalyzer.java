package su.geocaching.android.controller.selectlogic.geocachegroup;

import android.graphics.Point;
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
    private List<GeoCacheOverlayItem> overlayItemList;
    private MapView map;

    private static final int MINIMUM_GROUP_SIZE_TO_CREATE_CLUSTER = 2;
    private static final int FINGER_SIZE_X = 60;
    private static final int FINGER_SIZE_Y = 80;

    public GeoCacheListAnalyzer(MapView map) {
        this.map = map;
    }

    private void fillOverlayItemList(List<Centroid> centroidList) {
        overlayItemList = new LinkedList<GeoCacheOverlayItem>();
        for (Centroid centroid : centroidList) {
            int num = centroid.getNumberOfView();
            if (num != 0) {
                if (num < MINIMUM_GROUP_SIZE_TO_CREATE_CLUSTER) {
                    // we think minimum = 2
                    overlayItemList.add(new GeoCacheOverlayItem(centroid.getCache(), "", "", map.getContext()));
                } else {
                    overlayItemList.add(new GeoCacheOverlayItem(map.getProjection().fromPixels(centroid.getX(), centroid.getY()), "Group", "", map.getContext()));
                }
            }
        }
    }

    private List<Centroid> generateCentroids() {
        int sizeX = map.getWidth() / FINGER_SIZE_X;
        int sizeY = map.getHeight() / FINGER_SIZE_Y;
        List<Centroid> centroids = new LinkedList<Centroid>();

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                centroids.add(new Centroid((int) ((i + 0.5) * FINGER_SIZE_X), (int) ((j + 0.5) * FINGER_SIZE_Y), null));
            }
        }
        return centroids;
    }

    public List<GeoCacheOverlayItem> getList(List<GeoCache> geoCacheList) {
        List<Centroid> centroids = generateCentroids();
        List<GeoCacheView> points = generatePointsList(geoCacheList);

        List<Centroid> centroidList = new KMeans(points, centroids).getCentroids();

        fillOverlayItemList(centroidList);
        return overlayItemList;
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
