package su.geocaching.android.ui.test;

import junit.framework.TestSuite;

/**
 * 
 * @author Nikita Bumakov
 * 
 */
public class GeocachingTestSuite extends TestSuite {
	public GeocachingTestSuite() {
		addTestSuite(CompasHelperTest.class);
		addTestSuite(GeoCacheSaxHandlerTest.class);
	}
}
