package su.geocaching.android.ui.selectgeocache.timer;

import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.timer.tasks.CheckMapStateTimerTask;
import su.geocaching.android.ui.selectgeocache.timer.tasks.CheckRequestTimerTask;
import su.geocaching.android.ui.selectgeocache.timer.tasks.CheckTouchTimerTask;

import java.util.Timer;

/**
 * @author Yuri Denison
 * @date 25.11.10 19:47
 */
public class MapUpdateTimer extends Timer{
    private static final int REQUEST_TIMER_DELAY = 100;
    private static final int REQUEST_TIMER_PERIOD = 2000;
    private static final int TOUCH_TIMER_DELAY = 0;
    private static final int TOUCH_TIMER_PERIOD = 500;
    private static final int MAP_STATE_TIMER_DELAY = 0;
    private static final int MAP_STATE_TIMER_PERIOD = 700;

    public MapUpdateTimer(SelectGeoCacheMap map) {
        State state = new State(map);

        this.schedule(new CheckRequestTimerTask(state), REQUEST_TIMER_DELAY, REQUEST_TIMER_PERIOD);
        this.schedule(new CheckTouchTimerTask(state, map), TOUCH_TIMER_DELAY, TOUCH_TIMER_PERIOD);
        this.schedule(new CheckMapStateTimerTask(state, map, map.getCenter(), map.getZoom()),
                MAP_STATE_TIMER_DELAY, MAP_STATE_TIMER_PERIOD);
    }
}
