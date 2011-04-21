package su.geocaching.android.controller.selectmap.mapupdatetimer;

import su.geocaching.android.ui.selectmap.SelectMap;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class State {
    private boolean mapUpdated;
    private boolean requestSent;
    private final SelectMap gcMap;


    public State(SelectMap gcMap) {
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
            gcMap.updateCacheOverlay();

            mapUpdated = false;
            requestSent = false;
        }
    }
}
