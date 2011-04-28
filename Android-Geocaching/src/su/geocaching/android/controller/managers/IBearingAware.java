package su.geocaching.android.controller.managers;

/**
 * Interface of activities uses compass sensor
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 */
public interface IBearingAware {
    /**
     * @param bearing - new bearing in degrees
     */
    public void updateBearing(float bearing);
}
