package su.geocaching.android.ui.selectgeocache.geocachegroup;


import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class KMeans {
    private List<GeoCacheView> centroids;
    private HashMap<GeoCacheView, List<GeoCacheView>> resultMap;
    private List<GeoCacheView> points;
    private boolean ready;

    private static final String TAG = "KMeans";

    public KMeans(List<GeoCacheView> cacheCoordinates, List<GeoCacheView> centroids) {
        this.points = cacheCoordinates;
        this.centroids = centroids;
        Log.d(TAG, "Results Map init");
        initResultsMap();

        ready = false;
        int iteration = 1;
        while (!ready) {
            Log.d(TAG, "Iteration " + iteration + " started.");
            ready = true;
            fillCentroids();
            Log.d(TAG, "Centroids filled.");
            fillCurrentResultMap();
            Log.d(TAG, "Results Map filled. Iteration " + iteration + " finished.");
        }
    }

    public HashMap<GeoCacheView, List<GeoCacheView>> getClusterMap() {
        return resultMap;
    }

    private GeoCacheView findClosestCentroid(GeoCacheView point) {
        double minDistance = 10000;
        GeoCacheView result = null;
        for (GeoCacheView centroid : centroids) {
            double dist = countDistance(point, centroid);
            if (dist < minDistance) {
                result = centroid;
                minDistance = dist;
            }
        }
        return result;
    }

    private void initResultsMap() {
        resultMap = new HashMap<GeoCacheView, List<GeoCacheView>>();
        for (GeoCacheView centroid : centroids) {
            resultMap.put(centroid, new LinkedList<GeoCacheView>());
        }

        for (GeoCacheView point : points) {
            point.setClosestCentroid(findClosestCentroid(point));
            resultMap.get(point.getClosestCentroid()).add(point);
        }
    }

    private void fillCurrentResultMap() {
        for (GeoCacheView aPoint : points) {
            GeoCacheView newClosestCentroid = findClosestCentroid(aPoint);
            if (!aPoint.getClosestCentroid().equals(newClosestCentroid)) {
                ready = false;
                resultMap.get(aPoint.getClosestCentroid()).remove(aPoint);
                aPoint.setClosestCentroid(newClosestCentroid);
                resultMap.get(aPoint.getClosestCentroid()).add(aPoint);
            }
        }
    }

    private void fillCentroids() {
        for (GeoCacheView centroid : centroids) {
            if (resultMap.get(centroid).size() != 0) {
                Pair<Integer, Integer> newCentroid = getMassCenter(resultMap.get(centroid));
                centroid.setX(newCentroid.first);
                centroid.setY(newCentroid.second);
            } else {
                Log.d(TAG, "Empty centroid: x = " + centroid.getX() + ", y = " + centroid.getY());
            }
        }
    }

    private Pair<Integer, Integer> getMassCenter(List<GeoCacheView> points) {
        int centerX = 0, centerY = 0;
        for (GeoCacheView point : points) {
            centerX += point.getX();
            centerY += point.getY();
        }
        centerX /= points.size();
        centerY /= points.size();
        return new Pair<Integer, Integer>(centerX, centerY);
    }

    private double countDistance(GeoCacheView point, GeoCacheView centroid) {
        return Math.sqrt(Math.pow(point.getX() - centroid.getX(), 2) + Math.pow(point.getY() - centroid.getY(), 2));
    }
}
