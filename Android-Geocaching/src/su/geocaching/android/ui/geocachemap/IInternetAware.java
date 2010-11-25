package su.geocaching.android.ui.geocachemap;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @Nov 22, 2010
 *      <p>
 *      describes something what use internet connection
 *      </p>
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
