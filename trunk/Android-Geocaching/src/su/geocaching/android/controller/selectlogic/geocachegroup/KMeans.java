package su.geocaching.android.controller.selectlogic.geocachegroup;


import java.util.List;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class KMeans {
    private final List<Centroid> centroids;
    private final List<GeoCacheView> points;
    private boolean ready;
    private int iterations = 0;

    public KMeans(List<GeoCacheView> cacheCoordinates, List<Centroid> centroids) {
        this.points = cacheCoordinates;
        this.centroids = centroids;
        initResultsMap();

        ready = false;
        while (!ready) {
            iterations++;
            ready = true;
            fillCentroids();
            fillCurrentResult();
        }

    }

    public List<Centroid> getCentroids() {
        for (GeoCacheView point : points) {
            Centroid centroid = point.getClosestCentroid();
            centroid.plusNew(point.getX(), point.getY());
            if (centroid.getCache() == null) {
                centroid.setCache(point.getCache());
            }
        }
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
        }
    }

    private void fillCurrentResult() {
        for (GeoCacheView aPoint : points) {
            Centroid newClosestCentroid = findClosestCentroid(aPoint);
            if (!aPoint.getClosestCentroid().equals(newClosestCentroid)) {
                ready = false;
                aPoint.setClosestCentroid(newClosestCentroid);
            }
        }
    }

    private void fillCentroids() {
        for (GeoCacheView point : points) {
            point.getClosestCentroid().plusNew(point.getX(), point.getY());
        }
        for (Centroid centroid : centroids) {
            centroid.setNew();
        }
    }

    private long countDistance(GeoCacheView point, GeoCacheView centroid) {
        int x = point.getX() - centroid.getX();
        int y = point.getY() - centroid.getY();
        return x * x + y * y;
    }

    public int getIterations() {
        return iterations;
    }
}
