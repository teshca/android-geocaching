package su.geocaching.android.ui.searchgeocache;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 10, 2010
 * @description Interface of activities uses compass sensor
 */
public interface ICompassAware {
    /**
     * @param azimuth
     *            - new azimuth
     */
    public void updateBearing(int azimuth);

    /**
     * @return true if available orientation sensor
     */
    public boolean isCompassAvailable();
}
