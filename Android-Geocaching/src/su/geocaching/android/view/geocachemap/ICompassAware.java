package su.geocaching.android.view.geocachemap;

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
    public void updateAzimuth(float azimuth);
}
