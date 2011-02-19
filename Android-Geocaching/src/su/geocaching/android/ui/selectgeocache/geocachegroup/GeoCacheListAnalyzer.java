package su.geocaching.android.ui.selectgeocache.geocachegroup;

import android.graphics.Point;
import android.util.Log;
import com.google.android.maps.MapView;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison
 * @since 17.02.11
 */

public class GeoCacheListAnalyzer {
    private List<GeoCache> geoCacheList;
    private List<GeoCacheOverlayItem> overlayItemList;
    private MapView map;
    private HashMap<Pair, GeoCache> cacheCoordinatesMap;

    private static final int MINIMUM_GROUP_SIZE_TO_CREATE_CLUSTER = 2;
    private static final int FINGER_SIZE_X = 60;
    private static final int FINGER_SIZE_Y = 80;

    private static GeoCacheListAnalyzer instance;

    public GeoCacheListAnalyzer(MapView map) {
        this.map = map;
    }

    private Pair[] convertToPairArray(Object[] objects) {
        Pair[] pairs = new Pair[objects.length];
        for (int i = 0; i < objects.length; i++) {
            pairs[i] = (Pair) objects[i];
        }
        return pairs;
    }

    private void fillOverlayItemList(HashMap<Pair, List<Pair>> clusterPointMap) {
        for (Pair centroid : clusterPointMap.keySet()) {
            List<GeoCache> cacheList = convertPairArrayToGeoCacheList(clusterPointMap.get(centroid));
            if (cacheList.size() != 0) {
                if (cacheList.size() < MINIMUM_GROUP_SIZE_TO_CREATE_CLUSTER) {
                    for (GeoCache cache : cacheList) {
                        overlayItemList.add(new GeoCacheOverlayItem(cache, "", "", map.getContext()));
                    }
                } else {
                    overlayItemList.add(new GeoCacheOverlayItem(map.getProjection().fromPixels(centroid.x, centroid.y), cacheList, "Group", "", map.getContext()));
                }
            }
        }
    }

    private List<GeoCache> convertPairArrayToGeoCacheList(List<Pair> cacheCoordinates) {
        List<GeoCache> cacheList = new LinkedList<GeoCache>();
        for (Pair coordinates : cacheCoordinates) {
            for (Pair key : cacheCoordinatesMap.keySet()) {
                if (coordinates.equals(key)) {
                    cacheList.add(cacheCoordinatesMap.get(key));
                    break;
                }
            }
        }
        return cacheList;
    }

    private Pair[] generateCentroidsArray() {
        Pair size = new Pair(map.getWidth() / FINGER_SIZE_X, map.getHeight() / FINGER_SIZE_Y);
        Log.d("Screen Size", "width = " + map.getWidth() + ", height = " + map.getHeight());
        Pair[] centroids = new Pair[size.x * size.y];

        for (int i = 0; i < size.x; i++) {
            for (int j = 0; j < size.y; j++) {
                centroids[j * size.x + i] = new Pair(
                    (int) ((i + 0.5) * FINGER_SIZE_X),
                    (int) ((j + 0.5) * FINGER_SIZE_Y)
                );
            }
        }
        return centroids;
    }

    private HashMap<Pair, GeoCache> generateGeoCacheCoordinatesMap() {
        HashMap<Pair, GeoCache> coordinatesMap = new HashMap<Pair, GeoCache>();
        for (GeoCache cache : geoCacheList) {
            Point point = map.getProjection().toPixels(cache.getLocationGeoPoint(), null);
            coordinatesMap.put(new Pair(point.x, point.y), cache);
        }
        return coordinatesMap;
    }


    public List<GeoCacheOverlayItem> getList(List<GeoCache> geoCacheList) {
        this.geoCacheList = geoCacheList;
        overlayItemList = new LinkedList<GeoCacheOverlayItem>();

        cacheCoordinatesMap = generateGeoCacheCoordinatesMap();
        Pair[] centroids = generateCentroidsArray();
        HashMap<Pair, List<Pair>> clusterPointMap = new KMeans(convertToPairArray(cacheCoordinatesMap.keySet().toArray()), centroids).getClusterMap();
        fillOverlayItemList(clusterPointMap);
        return overlayItemList;
    }
}
