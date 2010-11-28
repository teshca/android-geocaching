package su.geocaching.android.ui.selectgeocache.timer.tasks;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.timer.State;
import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @date 25.11.10 19:45
 */
public class CheckMapStateTimerTask extends TimerTask{
    private GeoPoint lastCenter;
    private int lastZoom;
    private SelectGeoCacheMap map;
    private State state;

    private boolean mapUpdateRunning;

    public CheckMapStateTimerTask(State state, SelectGeoCacheMap map, GeoPoint center, int zoom) {
        lastZoom = zoom;
        lastCenter = center;
        mapUpdateRunning = false;
        this.map = map;
        this.state = state;
    }

    @Override
    public void run() {
        int currentZoom = map.getZoom();
        GeoPoint currentCenter = map.getCenter();
        if(lastZoom != currentZoom || !lastCenter.equals(currentCenter)) {
            mapUpdateRunning = true;
        } else {
            if(!mapUpdateRunning) {
                state.setMapUpdatedTrue();
            }
            mapUpdateRunning = false;
        }
        lastZoom = currentZoom;
        lastCenter = currentCenter;
    }
}
