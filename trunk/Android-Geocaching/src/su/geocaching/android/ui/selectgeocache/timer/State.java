package su.geocaching.android.ui.selectgeocache.timer;

import android.util.Log;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class State {
    private boolean mapUpdated;
    private boolean requestSent;
    private SelectGeoCacheMap gcMap;


    public State(SelectGeoCacheMap gcMap) {
        this.mapUpdated = false;
        this.requestSent = false;
        this.gcMap = gcMap;
    }

    public synchronized void setMapUpdatedTrue() {
        mapUpdated = true;
        sendRequest();
    }

    public synchronized void setRequestSentTrue() {
        requestSent = true;
        sendRequest();
    }

    private synchronized void sendRequest() {
        if (requestSent && mapUpdated) {
            Log.d("mapStats", "state start");
            gcMap.updateCacheOverlay();

            mapUpdated = false;
            requestSent = false;
        }
    }
}
