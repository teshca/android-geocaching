package su.geocaching.android.ui.selectgeocache.geocachegroup;


import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class KMeans {
    private List<Pair> centroids;
    private HashMap<Pair, List<Pair>> resultMap;
    private List<AdvancedPair> points;
    private boolean ready;

    public KMeans(Pair[] cacheCoordinates, Pair[] centroids) {
        this.points = convertPairArray(cacheCoordinates);
        this.centroids = Arrays.asList(centroids);
        initResultsMap();

        ready = false;
        while (!ready) {
            ready = true;
            fillCentroids();
            fillCurrentResultMap();
        }
    }

    public HashMap<Pair, List<Pair>> getClusterMap() {
        return resultMap;
    }

    private List<AdvancedPair> convertPairArray(Pair[] pairs) {
        List<AdvancedPair> res = new LinkedList<AdvancedPair>();
        for (int i = 0; i < pairs.length; i++) {
            res.add(new AdvancedPair(pairs[i].x, pairs[i].y));
        }
        return res;
    }

    private Pair getClosestCentroid(AdvancedPair point) {
        double minDistance = 10000;
        Pair result = null;
        for (Pair centroid : centroids) {
            double dist = countDistance(point, centroid);
            if (dist < minDistance) {
                result = centroid;
                minDistance = dist;
            }
        }
        return result;
    }

    private void initResultsMap() {
        resultMap = new HashMap<Pair, List<Pair>>();
        for (Pair centroid : centroids) {
            resultMap.put(centroid, new LinkedList<Pair>());
        }

        for (AdvancedPair point : points) {
            point.closestCentroid = getClosestCentroid(point);
            resultMap.get(point.closestCentroid).add(point);
        }
    }

    private void fillCurrentResultMap() {
        for (AdvancedPair aPoint : points) {
            Pair newClosestCentroid = getClosestCentroid(aPoint);
            if (!aPoint.closestCentroid.equals(newClosestCentroid)) {
                ready = false;
                resultMap.get(aPoint.closestCentroid).remove(aPoint);
                aPoint.closestCentroid = newClosestCentroid;
                resultMap.get(aPoint.closestCentroid).add(aPoint);
            }
        }
    }

    private void fillCentroids() {
        for (Pair centroid : centroids) {
            if (resultMap.get(centroid).size() != 0) {
                Pair newCentroid = getMassCenter(resultMap.get(centroid));
                centroid.x = newCentroid.x;
                centroid.y = newCentroid.y;
            } else {
                Log.d("Centroid fill", "Empty centroid: x = " + centroid.x + ", y = " + centroid.y);
//                centroids.remove(centroid);
            }
        }
    }

    private Pair getMassCenter(List<Pair> points) {
        int centerX = 0, centerY = 0;
        for (Pair point : points) {
            centerX += point.x;
            centerY += point.y;
        }
        centerX /= points.size();
        centerY /= points.size();
        return new Pair(centerX, centerY);
    }

    private double countDistance(Pair point, Pair centroid) {
        return Math.sqrt(Math.pow(point.x - centroid.x, 2) + Math.pow(point.y - centroid.y, 2));
    }

    private class AdvancedPair extends Pair {
        public Pair closestCentroid = null;

        public AdvancedPair(int x, int y) {
            super(x, y);
        }
    }
}
