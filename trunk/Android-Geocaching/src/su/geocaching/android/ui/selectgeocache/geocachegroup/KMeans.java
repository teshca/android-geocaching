package su.geocaching.android.ui.selectgeocache.geocachegroup;


import android.util.Log;

import java.util.List;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class KMeans {
    private List<Centroid> centroids;
    private List<GeoCacheView> points;
    private boolean ready;
    private long timeStart;

    private static final String TAG = "KMeans";

    public KMeans(List<GeoCacheView> cacheCoordinates, List<Centroid> centroids) {
        timeStart = System.currentTimeMillis();
        this.points = cacheCoordinates;
        this.centroids = centroids;
        long iterations = 0;
        initResultsMap();

        ready = false;
        while (!ready) {
            iterations++;
            ready = true;
            fillCentroids();
            fillCurrentResult();
        }
        Log.d("mapStats", "iterations = " + iterations);

    }

    public List<Centroid> getClusterMap() {
        Log.d("mapStats", "timeAlgorithm = " + (System.currentTimeMillis() - timeStart));
        return centroids;
    }

    private Centroid findClosestCentroid(GeoCacheView point) {
        long minDistance = (long) 1e7;
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
                Pair newCentroid = getMassCenter(centroid.getGeoCacheList());
                centroid.setX(newCentroid.x);
                centroid.setY(newCentroid.y);
            }
        }
    }

    private Pair getMassCenter(List<GeoCacheView> points) {
        int centerX = 0, centerY = 0;
        for (GeoCacheView point : points) {
            centerX += point.getX();
            centerY += point.getY();
        }
        centerX /= points.size();
        centerY /= points.size();
        return new Pair(centerX, centerY);
    }

    private long countDistance(GeoCacheView point, GeoCacheView centroid) {
        int x = point.getX() - centroid.getX();
        int y = point.getY() - centroid.getY();
        return x * x + y * y;
    }

    private class Pair {
        public int x;
        public int y;

        private Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
