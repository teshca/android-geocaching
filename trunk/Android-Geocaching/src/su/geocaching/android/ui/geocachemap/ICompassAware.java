package su.geocaching.android.ui.geocachemap;

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
    public void updateAzimuth(int azimuth);

    /**
     * @return true if available orientation sensor
     */
    public boolean isCompassAvailable();
}
