package su.geocaching.android.ui.selectgeocache.geocachegroup;


import java.util.List;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class KMeans {
    private List<Centroid> centroids;
    private List<GeoCacheView> points;
    private boolean ready;

    public KMeans(List<GeoCacheView> cacheCoordinates, List<Centroid> centroids) {
        this.points = cacheCoordinates;
        this.centroids = centroids;
        initResultsMap();

        ready = false;
        while (!ready) {
            ready = true;
            fillCentroids();
            fillCurrentResult();
        }

    }

    public List<Centroid> getCentroids() {
        return centroids;
    }

    private Centroid findClosestCentroid(GeoCacheView point) {
        long minDistance = Long.MAX_VALUE;
        Centroid result = null;
        for (Centroid centroid : centroids) {
            long dist = countDistance(point, centroid);
            if (dist < minDistance) {
                result = centroid;
                minDistance = dist;
            }
        }
        return result;
    }

    private void initResultsMap() {
        for (GeoCacheView point : points) {
            Centroid centroid = findClosestCentroid(point);
            point.setClosestCentroid(centroid);
            centroid.getGeoCacheList().add(point);
        }
    }

    private void fillCurrentResult() {
        for (GeoCacheView aPoint : points) {
            Centroid newClosestCentroid = findClosestCentroid(aPoint);
            if (!aPoint.getClosestCentroid().equals(newClosestCentroid)) {
                ready = false;
                aPoint.getClosestCentroid().getGeoCacheList().remove(aPoint);
                aPoint.setClosestCentroid(newClosestCentroid);
                aPoint.getClosestCentroid().getGeoCacheList().add(aPoint);
            }
        }
    }

    private void fillCentroids() {
        for (Centroid centroid : centroids) {
            if (centroid.getGeoCacheList().size() != 0) {
                updateCentroid(centroid.getGeoCacheList(), centroid);
            }
        }
    }

    private void updateCentroid(List<GeoCacheView> points, Centroid centroid) {
        int centerX = 0, centerY = 0;
        for (GeoCacheView point : points) {
            centerX += point.getX();
            centerY += point.getY();
        }
        int size = points.size();
        centerX /= size;
        centerY /= size;
        centroid.set(centerX, centerY);
    }

    private long countDistance(GeoCacheView point, GeoCacheView centroid) {
        int x = point.getX() - centroid.getX();
        int y = point.getY() - centroid.getY();
        return x * x + y * y;
    }
}
