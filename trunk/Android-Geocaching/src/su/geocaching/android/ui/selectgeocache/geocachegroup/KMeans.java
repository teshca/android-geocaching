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
    private long timeStart;

    private static final String TAG = "KMeans";

    public KMeans(List<GeoCacheView> cacheCoordinates, List<GeoCacheView> centroids) {
        timeStart = System.currentTimeMillis();
        this.points = cacheCoordinates;
        this.centroids = centroids;
        long iterations = 0;
        Log.d(TAG, "Results Map init");
        initResultsMap();

        ready = false;
        while (!ready) {
            iterations++;
            ready = true;
            fillCentroids();
            Log.d(TAG, "Centroids filled.");
            fillCurrentResultMap();
        }
        Log.d("mapStats", "iterations = " + iterations);

    }

    public HashMap<GeoCacheView, List<GeoCacheView>> getClusterMap() {
        Log.d("mapStats", "timeAlgorithm = " + (System.currentTimeMillis() - timeStart));
        return resultMap;
    }

    private GeoCacheView findClosestCentroid(GeoCacheView point) {
        long minDistance = 10000;
        GeoCacheView result = null;
        for (GeoCacheView centroid : centroids) {
            long dist = countDistance(point, centroid);
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
        //deleteEmptyCentroids();
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
            }
        }
    }

    private void deleteEmptyCentroids() {
        for (int i = 0; i < centroids.size(); i++) {
            if (resultMap.get(centroids.get(i)).size() == 0) {
                centroids.remove(i);
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

    private long countDistance(GeoCacheView point, GeoCacheView centroid) {
        int x = point.getX() - centroid.getX();
        int y = point.getY() - centroid.getY();
        return x * x + y * y;
    }
}
