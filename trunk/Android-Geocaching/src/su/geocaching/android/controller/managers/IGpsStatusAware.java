package su.geocaching.android.controller.managers;

/**
 * Interface of activity which need to get updates of gps status
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 25, 2010
 */
public interface IGpsStatusAware {
    /**
     * @param status - show status to user
     */
    public void updateStatus(String status);
}
