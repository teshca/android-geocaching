package su.geocaching.android.controller.compass;

/**
 * Helper class for calculations related to the compass
 * 
 * @author Nikita Bumakov
 */
public class CompassHelper {

    /**
     * Calculate azimuth to direct bearing
     * 
     * @param angle
     *            - bearing to north
     * @return azimuth string (122.2° for example)
     */
    public static String degreesToString(float angle) {
        angle = normalizeAngle(angle);
        if (angle < 0) {
            angle = 360 + angle;
        }
        return String.format("%.1f°", 360 - angle);
    }

    /**
     * Calculate difference between lastDirection and currentDirection
     * 
     * @param lastDirection
     *            //TODO describe it
     * @param currentDirection
     *            //TODO describe it
     * @return normalize angle between lastDirection and currentDirection
     */
    public static float calculateNormalDifference(float lastDirection, float currentDirection) {
        float difference = currentDirection - lastDirection;
        return normalizeAngle(difference);
    }

    /**
     * Normalize angle
     * 
     * @param angle
     *            //TODO describe it
     * @return //TODO describe it
     */
    public static float normalizeAngle(float angle) {
        angle %= 360;
        if (angle < -180) {
            angle += 360;
        }
        if (angle > 180) {
            angle -= 360;
        }
        return angle;
    }
}
