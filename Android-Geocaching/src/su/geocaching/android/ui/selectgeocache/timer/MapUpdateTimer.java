package su.geocaching.android.ui.selectgeocache.timer;

import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.timer.tasks.CheckMapStateTimerTask;
import su.geocaching.android.ui.selectgeocache.timer.tasks.CheckRequestTimerTask;

import java.util.Timer;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class MapUpdateTimer extends Timer {
    private static final int REQUEST_TIMER_DELAY = 50;
    private static final int REQUEST_TIMER_PERIOD = 400;
    private static final int MAP_STATE_TIMER_DELAY = 0;
    private static final int MAP_STATE_TIMER_PERIOD = 400;

    public MapUpdateTimer(SelectGeoCacheMap map) {
        State state = new State(map);

        this.schedule(new CheckRequestTimerTask(state), REQUEST_TIMER_DELAY, REQUEST_TIMER_PERIOD);
        this.schedule(new CheckMapStateTimerTask(state, map, map.getCenter(), map.getZoom()),
                MAP_STATE_TIMER_DELAY, MAP_STATE_TIMER_PERIOD);
    }
}
