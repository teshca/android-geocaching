package su.geocaching.android.controller.selectmap.geocachegroup;

import android.graphics.Point;
import android.os.AsyncTask;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.GeoPoint;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison
 * @since 17.02.11
 */

public class GeoCacheListAnalyzer {
    private Projection projection;
    private int mapWidth, mapHeight;

    private static final int FINGER_SIZE_X = 60;
    private static final int FINGER_SIZE_Y = 80;

    public GeoCacheListAnalyzer(Projection projection, int mapWidth, int mapHeight) {
        this.projection = projection;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    private LinkedList<GeoCache> createGeocachesList(List<Centroid> centroidList) {
        final LinkedList<GeoCache> geocachesList = new LinkedList<GeoCache>();
        for (Centroid centroid : centroidList) {
            int num = centroid.getNumberOfView();
            if (num > 0) {
                if (num == 1) {
                    geocachesList.add(centroid.getCache());
                } else {
                    Point screenLocation = new Point(centroid.getX(), centroid.getY());
                    LatLng location = projection.fromScreenLocation(screenLocation);
                    GeoPoint geoPoint = new GeoPoint(location.latitude, location.longitude);
                    GeoCache group = new GeoCache();
                    group.setGeoPoint(geoPoint);
                    group.setType(GeoCacheType.GROUP);
                    geocachesList.add(group);
                }
            }
        }
        return geocachesList;
    }

    private List<Centroid> generateCentroids() {
        final int sizeX = mapWidth / FINGER_SIZE_X;
        final int sizeY = mapHeight / FINGER_SIZE_Y;
        List<Centroid> centroids = new LinkedList<Centroid>();

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                centroids.add(new Centroid((int) ((i + 0.5) * FINGER_SIZE_X), (int) ((j + 0.5) * FINGER_SIZE_Y), null));
            }
        }
        return centroids;
    }

    public List<GeoCache> getGroupedList(List<GeoCache> geoCacheList, AsyncTask<?,?,?> asyncTask) {
        final List<Centroid> centroids = generateCentroids();
        if (asyncTask.isCancelled()) return null;
        final List<GeoCacheView> points = generatePointsList(geoCacheList);
        if (asyncTask.isCancelled()) return null;
        final List<Centroid> centroidList = new KMeans(points, centroids, asyncTask).getCentroids();
        if (asyncTask.isCancelled()) return null;
        return createGeocachesList(centroidList);
    }

    private List<GeoCacheView> generatePointsList(List<GeoCache> geoCacheList) {
        List<GeoCacheView> list = new LinkedList<GeoCacheView>();
        for (GeoCache cache : geoCacheList) {
            LatLng latLng = new LatLng(cache.getGeoPoint().getLatitude(), cache.getGeoPoint().getLongitude());
            Point point = projection.toScreenLocation(latLng);
            list.add(new GeoCacheView(point.x, point.y, cache));
        }
        return list;
    }
}
