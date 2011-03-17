package su.geocaching.android.ui.test;

import android.content.ContentResolver;
import android.test.AndroidTestCase;
import android.util.Log;
import su.geocaching.android.controller.compass.CompassHelper;

/**
 * @author Nikita Bumakov
 */
public class CompassHelperTest extends AndroidTestCase {

    public static final String LOG_TAG = "GeoCaching.su - Test";

    ContentResolver contentResolver;

    @Override
    protected void setUp() throws Exception {
        contentResolver = getContext().getContentResolver();
        super.setUp();
    }

    public void testNormalizeAngle() {
        float angle = 480;
        float expectedAngle = 120;
        float actualAngle = CompassHelper.normalizeAngle(angle);
        assertEquals(expectedAngle, actualAngle);
    }

    public void testNormalizeAngle2() {
        float angle = 330;
        float expectedAngle = -30;
        float actualAngle = CompassHelper.normalizeAngle(angle);
        assertEquals(expectedAngle, actualAngle);
    }

    public void testNormalizeAngle3() {
        float angle = -30;
        float expectedAngle = -30;
        float actualAngle = CompassHelper.normalizeAngle(angle);
        assertEquals(expectedAngle, actualAngle);
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }

    @Override
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
        Log.d(LOG_TAG, "testAndroidTestCaseSetupProperly");
    }
}
