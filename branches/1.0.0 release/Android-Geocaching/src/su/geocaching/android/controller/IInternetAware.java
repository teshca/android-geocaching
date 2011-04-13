package su.geocaching.android.controller;

/**
 * describes something what use internet connection
 *
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Nov 22, 2010
 */
public interface IInternetAware {

    /**
     * Called when internet has been lost
     */
    public void onInternetLost();

    /**
     * Called when internet has been appeared
     */
    public void onInternetFound();

}
