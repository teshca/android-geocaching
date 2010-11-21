package su.geocaching.android.ui.geocachemap;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 20, 2010
 * @Description Describes methods which must have been overridden in activities
 *              uses map
 */
public interface IMapAware {
    /**
     * @param item
     *            which was taped
     */
    public void onGeoCacheItemTaped(GeoCacheOverlayItem item);
}
