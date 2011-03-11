package su.geocaching.android.ui.selectgeocache.geocachegroup;

import android.graphics.Point;
import com.google.android.maps.MapView;

import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
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

    private void fillOverlayItemList(List<Centroid> clusterPointMap) {
        overlayItemList = new LinkedList<GeoCacheOverlayItem>();
        for (Centroid centroid : clusterPointMap) {
            List<GeoCacheView> cacheList = centroid.getGeoCacheList();
            if (cacheList.size() != 0) {
                if (cacheList.size() < MINIMUM_GROUP_SIZE_TO_CREATE_CLUSTER) {
                    for (GeoCacheView cache : cacheList) {
                        overlayItemList.add(new GeoCacheOverlayItem(cache.getCache(), "", "", map.getContext()));
                    }
                } else {
                    overlayItemList.add(new GeoCacheOverlayItem(map.getProjection().fromPixels(centroid.getX(), centroid.getY()),
                            getCacheListFromCacheViewList(cacheList),
                            "Group", "",
                            map.getContext()));
                }
            }
        }
    }

    private List<GeoCache> getCacheListFromCacheViewList(List<GeoCacheView> cacheViewList) {
        List<GeoCache> list = new LinkedList<GeoCache>();
        for (GeoCacheView cacheView : cacheViewList) {
            list.add(cacheView.getCache());
        }
        return list;
    }

    private List<Centroid> generateCentroidsArray() {
        int sizeX = map.getWidth() / FINGER_SIZE_X;
        int sizeY = map.getHeight() / FINGER_SIZE_Y;
        LogManager.d("Screen Size", "width = " + map.getWidth() + ", height = " + map.getHeight());
        List<Centroid> centroids = new LinkedList<Centroid>();

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                centroids.add(new Centroid(
                        (int) ((i + 0.5) * FINGER_SIZE_X),
                        (int) ((j + 0.5) * FINGER_SIZE_Y),
                        null
                ));
            }
        }
        return centroids;
    }

    public List<GeoCacheOverlayItem> getList(List<GeoCache> geoCacheList) {
        List<Centroid> centroids = generateCentroidsArray();
        List<GeoCacheView> points = generatePointsList(geoCacheList);

        long startTime = System.currentTimeMillis();
        List<Centroid> clusterPointMap = new KMeans(points, centroids).getClusterMap();
        LogManager.d("mapStats", "log: sizeList = " + geoCacheList.size() + " centroids = " + centroids.size() + " time = " + (System.currentTimeMillis() - startTime));

        fillOverlayItemList(clusterPointMap);
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
