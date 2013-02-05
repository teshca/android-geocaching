package su.geocaching.android.ui.test;

import junit.framework.TestCase;
import su.geocaching.android.controller.compass.CompassHelper;

/**
 * @author Nikita Bumakov
 */
public class CompassHelperTest extends TestCase {

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
}
