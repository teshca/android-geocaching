package su.geocaching.android.ui.test;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Nikita Bumakov
 */
public class GeocachingTestSuite extends TestSuite {
    public GeocachingTestSuite() {
        //addTestSuite(KMeansTest.class);
        addTestSuite(CompassHelperTest.class);
        addTestSuite(SexagesimalTest.class);
        //addTestSuite(GeoCacheSaxHandlerTest.class);
    }

    public static Test suite() {
        return new GeocachingTestSuite();
    }
}
