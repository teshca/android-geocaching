package su.geocaching.android.controller.managers;

/**
 * describes something what use internet connection
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 22, 2010
 */
public interface IConnectionAware {

    /**
     * Called when internet has been lost
     */
    public void onConnectionLost();

    /**
     * Called when internet has been appeared
     */
    public void onConnectionFound();
}
