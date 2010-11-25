package su.geocaching.android.ui.selectgeocache.timer;

import android.util.Log;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * @author Yuri Denison
 * @date 25.11.10 21:01
 */
public class State {
    private boolean touch;
    private boolean map;
    private boolean request;
    private SelectGeoCacheMap gcMap;


    public State(SelectGeoCacheMap gcMap) {
        this.touch = false;
        this.map = false;
        this.request = false;
        this.gcMap = gcMap;
    }

    /**
     *
     * @param type
     *          1 - request, 2 - map, 3 - touch
     */
    public void setTrue(int type) {
        switch (type) {
            case 1:
                request = true; break;
            case 2:
                map = true; break;
            case 3:
                touch = true; break;
        }

        if(request && map && touch) {
            gcMap.updateCacheOverlay();

            touch = false;
            map = false;
            request = false;
        }
    }
}
