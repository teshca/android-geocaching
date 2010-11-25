package su.geocaching.android.ui.geocachemap;

/**
 * Describes methods which must have been overridden in activities uses map
 * 
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 20, 2010
 */
public interface IMapAware {
    /**
     * @param item
     *            which was taped
     */
    public void onGeoCacheItemTaped(GeoCacheOverlayItem item);
}
