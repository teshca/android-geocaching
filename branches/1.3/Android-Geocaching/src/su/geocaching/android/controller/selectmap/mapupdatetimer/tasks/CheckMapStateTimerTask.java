package su.geocaching.android.controller.selectmap.mapupdatetimer.tasks;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import su.geocaching.android.controller.selectmap.mapupdatetimer.MapUpdateTimerState;

import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class CheckMapStateTimerTask extends TimerTask {
    private GeoPoint lastCenter;
    private int lastZoom;
    private final MapView map;
    private final MapUpdateTimerState state;

    private boolean mapUpdateRunning;

    public CheckMapStateTimerTask(MapUpdateTimerState state, MapView map) {
        this.mapUpdateRunning = false;
        this.map = map;
        this.state = state;
    }

    @Override
    public void run() {
        int currentZoom = map.getZoomLevel();
        GeoPoint currentCenter = map.getMapCenter();
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