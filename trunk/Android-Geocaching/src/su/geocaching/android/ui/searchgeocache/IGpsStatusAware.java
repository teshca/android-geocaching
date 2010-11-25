package su.geocaching.android.ui.searchgeocache;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 25, 2010
 *      <p>
 *      Interface of activity which need to get updates of gps status
 *      </p>
 */
public interface IGpsStatusAware {
    /**
     * @param status
     *            - show status to user
     */
    public void updateStatus(String status, int type);
}
