package su.geocaching.android.ui.test;

import junit.framework.TestSuite;

/**
 * @author Nikita Bumakov
 */
public class GeocachingTestSuite extends TestSuite {
    public GeocachingTestSuite() {
        addTestSuite(KMeansTest.class);
        addTestSuite(CompassHelperTest.class);
        addTestSuite(GeoCacheSaxHandlerTest.class);
    }
}
