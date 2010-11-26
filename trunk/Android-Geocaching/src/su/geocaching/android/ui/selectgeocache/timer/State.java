package su.geocaching.android.ui.selectgeocache.timer;

import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * @author Yuri Denison
 * @date 25.11.10 21:01
 */
public class State {
    private boolean wasTouched;
    private boolean mapUpdated;
    private boolean requestSent;
    private SelectGeoCacheMap gcMap;


    public State(SelectGeoCacheMap gcMap) {
        this.wasTouched = false;
        this.mapUpdated = false;
        this.requestSent = false;
        this.gcMap = gcMap;
    }

    public synchronized void setTouchedTrue() {
        wasTouched = true;
        sendRequest();
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
        if(requestSent && mapUpdated && wasTouched) {
            gcMap.updateCacheOverlay();

            wasTouched = false;
            mapUpdated = false;
            requestSent = false;
        }
    }
}
