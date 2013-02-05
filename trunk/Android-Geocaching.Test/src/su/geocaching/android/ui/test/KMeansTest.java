package su.geocaching.android.ui.test;

import android.test.AndroidTestCase;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.selectmap.geocachegroup.Centroid;
import su.geocaching.android.controller.selectmap.geocachegroup.GeoCacheView;
import su.geocaching.android.controller.selectmap.geocachegroup.KMeans;
import su.geocaching.android.model.GeoCache;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: Yuri Denison
 * @since: 17.03.11
 */
public class KMeansTest extends AndroidTestCase {
    private static final int MAX_NUMBER_OF_VIEW = 5020;
    private static final int MIN_NUMBER_OF_VIEW = 10;
    private static final int STEP = 100;
    private static final int NUMBER_OF_TESTS = 5;
    private static final int SCREEN_WIDTH = 640;
    private static final int SCREEN_HEIGHT = 480;
    private static final int FINGER_SIZE_X = 80;
    private static final int FINGER_SIZE_Y = 60;
    private int iter = 0;
    public static final String TAG = "KMeans"; //KMeansTest.class.getName();


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testMultiply() {
        for (int i = MIN_NUMBER_OF_VIEW; i < MAX_NUMBER_OF_VIEW; i += STEP) {
            withNumberOfViewTest(i);
        }
    }

    public void withNumberOfViewTest(int numberOfView) {
        long value = 0, iteration = 0;
        for (int i = 0; i < NUMBER_OF_TESTS; i++) {
            value += singleTest(numberOfView);
            iteration += iter;
        }
        value /= NUMBER_OF_TESTS;
        iteration /= NUMBER_OF_TESTS;
        LogManager.d(TAG, numberOfView + ", " + iteration + ", " + value);
    }

    private List<GeoCacheView> generatePoints(int numberOfView) {
        List<GeoCacheView> points = new LinkedList<GeoCacheView>();
        for (int i = 0; i < numberOfView; i++) {
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

    private long singleTest(int numberOfView) {
        List<GeoCacheView> points = generatePoints(numberOfView);
        List<Centroid> centroids = generateCentroids();
        long startTime = System.currentTimeMillis();
        //KMeans k = new KMeans(points, centroids);
        //k.getCentroids();
        //iter = k.getIterations();
        long time = System.currentTimeMillis() - startTime;
        // LogManager.d(TAG, "" + time);
        return time;
    }
}
