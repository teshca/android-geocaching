package su.geocaching.android.ui.selectgeocache.timer;

import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * @author Yuri Denison
 * @date 25.11.10 21:01
 */
public class State {
    private boolean wasTouched;
    private boolean wasScrolledOrZoomed;
    private boolean requestSent;
    private SelectGeoCacheMap gcMap;


    public State(SelectGeoCacheMap gcMap) {
        this.wasTouched = false;
        this.wasScrolledOrZoomed = false;
        this.requestSent = false;
        this.gcMap = gcMap;
    }

    public void setTouchedTrue() {
        wasTouched = true;
        sendRequest();
    }

    public void setScrolledOrZoomedTrue() {
        wasScrolledOrZoomed = true;
        sendRequest();
    }

    public void setRequestSentTrue() {
        requestSent = true;
        sendRequest();
    }

    private void sendRequest() {
        if(requestSent && wasScrolledOrZoomed && wasTouched) {
            gcMap.updateCacheOverlay();

            wasTouched = false;
            wasScrolledOrZoomed = false;
            requestSent = false;
        }
    }
}
