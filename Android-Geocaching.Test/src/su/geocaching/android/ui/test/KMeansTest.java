package su.geocaching.android.ui.test;

import android.test.AndroidTestCase;
import android.util.Log;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.selectgeocache.geocachegroup.Centroid;
import su.geocaching.android.ui.selectgeocache.geocachegroup.GeoCacheView;
import su.geocaching.android.ui.selectgeocache.geocachegroup.KMeans;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: Yuri Denison
 * @since: 17.03.11
 */
public class KMeansTest extends AndroidTestCase {
    private static final int NUMBER_OF_VIEW = 2000;
    private static final int NUMBER_OF_TESTS = 10;
    private static final int SCREEN_WIDTH = 640;
    private static final int SCREEN_HEIGHT = 480;
    private static final int FINGER_SIZE_X = 60;
    private static final int FINGER_SIZE_Y = 80;
    public static final String TAG = "KMeans"; //KMeansTest.class.getName();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testKMeans() {
        int value = 0;
        for (int i = 0; i < NUMBER_OF_TESTS; i++) {
            value += singleTest();
        }
        value /= NUMBER_OF_TESTS;
        Log.d(TAG, "Time = " + value + " with items = " + NUMBER_OF_VIEW + "");
        assertEquals(1, 1);
    }

    private List<GeoCacheView> generatePoints() {
        List<GeoCacheView> points = new LinkedList<GeoCacheView>();
        for (int i = 0; i < NUMBER_OF_VIEW; i++) {
            points.add(
                    new GeoCacheView(
                            (int) (SCREEN_WIDTH * Math.random()),
                            (int) (SCREEN_HEIGHT * Math.random()),
                            new GeoCache()
                    ));
        }
        return points;
    }

    private List<Centroid> generateCentroids() {
        List<Centroid> centroids = new LinkedList<Centroid>();

        for (int i = 0; i < SCREEN_WIDTH / FINGER_SIZE_X; i++) {
            for (int j = 0; j < SCREEN_HEIGHT / FINGER_SIZE_Y; j++) {
                centroids.add(new Centroid(
                        (int) ((i + 0.5) * FINGER_SIZE_X),
                        (int) ((j + 0.5) * FINGER_SIZE_Y),
                        null
                ));
            }
        }
        return centroids;
    }

    private long singleTest() {
        List<GeoCacheView> points = generatePoints();
        List<Centroid> centroids = generateCentroids();
        long startTime = System.currentTimeMillis();
        new KMeans(points, centroids).getCentroids();
        long time = System.currentTimeMillis() - startTime;
        Log.d(TAG, "" + time);
        return time;
    }
}
