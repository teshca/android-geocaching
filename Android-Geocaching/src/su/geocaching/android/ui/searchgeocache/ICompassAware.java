package su.geocaching.android.ui.searchgeocache;

/**
 * Interface of activities uses compass sensor
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 */
public interface ICompassAware {
    /**
     * @param bearing
     *            - new bearing in degrees
     */
    public void updateBearing(int bearing);

    /**
     * @return true if available orientation sensor
     */
    public boolean isCompassAvailable();
}
