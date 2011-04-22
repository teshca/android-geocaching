package su.geocaching.android.controller.selectmap.mapupdatetimer;

import su.geocaching.android.controller.selectmap.mapupdatetimer.tasks.CheckMapStateTimerTask;
import su.geocaching.android.controller.selectmap.mapupdatetimer.tasks.CheckRequestTimerTask;
import su.geocaching.android.ui.selectmap.SelectMapActivity;

import java.util.Timer;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class MapUpdateTimer extends Timer {
    private static final int REQUEST_TIMER_DELAY = 50;
    private int REQUEST_TIMER_PERIOD = 400;
    private static final int MAP_STATE_TIMER_DELAY = 0;
    private static final int MAP_STATE_TIMER_PERIOD = 400;

    public MapUpdateTimer(SelectMapActivity map) {
        State state = new State(map);
        scheduleTasks(map, state);
    }

    public MapUpdateTimer(SelectMapActivity map, int requestPeriod) {
        State state = new State(map);
        this.REQUEST_TIMER_PERIOD = requestPeriod;
        scheduleTasks(map, state);
    }

    private void scheduleTasks(SelectMapActivity map, State state) {
        this.schedule(new CheckRequestTimerTask(state), REQUEST_TIMER_DELAY, REQUEST_TIMER_PERIOD);
        this.schedule(new CheckMapStateTimerTask(state, map, map.getCenter(), map.getZoom()),
                MAP_STATE_TIMER_DELAY, MAP_STATE_TIMER_PERIOD);
    }
}
