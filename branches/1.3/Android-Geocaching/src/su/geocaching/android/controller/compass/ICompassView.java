package su.geocaching.android.controller.compass;

/**
 * Interface provide compass updates
 *
 * @author Nikita Bumakov
 */
public interface ICompassView {

    /**
     * Set new direction to the north
     *
     * @param direction - direction to the north
     * @return returns TRUE if the drawing was successful, otherwise FALSE
     */
    public boolean setDirection(float direction);
    public void setSourceType(CompassSourceType sourceType);
}
