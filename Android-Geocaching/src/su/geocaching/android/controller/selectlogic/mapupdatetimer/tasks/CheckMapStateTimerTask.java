package su.geocaching.android.controller.selectlogic.mapupdatetimer.tasks;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.selectlogic.mapupdatetimer.State;
import su.geocaching.android.ui.selectmap.SelectMap;

import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class CheckMapStateTimerTask extends TimerTask {
    private GeoPoint lastCenter;
    private int lastZoom;
    private final SelectMap map;
    private final State state;

    private boolean mapUpdateRunning;

    public CheckMapStateTimerTask(State state, SelectMap map, GeoPoint center, int zoom) {
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
        if (lastZoom != currentZoom || !lastCenter.equals(currentCenter)) {
            mapUpdateRunning = true;
        } else {
            if (mapUpdateRunning) {
                state.setMapUpdatedTrue();
            }
            mapUpdateRunning = false;
        }
        lastZoom = currentZoom;
        lastCenter = currentCenter;
    }
}
