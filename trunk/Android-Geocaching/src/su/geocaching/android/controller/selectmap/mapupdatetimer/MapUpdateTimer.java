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
    private static final int REQUEST_TIMER_PERIOD = 400;
    private static final int MAP_STATE_TIMER_DELAY = 0;
    private static final int MAP_STATE_TIMER_PERIOD = 400;

    private CheckRequestTimerTask checkRequestTimerTask;
    private CheckMapStateTimerTask checkMapStateTimerTask;

    public MapUpdateTimer(SelectMapActivity map) {
        MapUpdateTimerState state = new MapUpdateTimerState(map);
        checkRequestTimerTask = new CheckRequestTimerTask(state);
        checkMapStateTimerTask = new CheckMapStateTimerTask(state, map);
    }

    public void scheduleTasks() {
        this.schedule(checkRequestTimerTask, REQUEST_TIMER_DELAY, REQUEST_TIMER_PERIOD);
        this.schedule(checkMapStateTimerTask, MAP_STATE_TIMER_DELAY, MAP_STATE_TIMER_PERIOD);
    }
}
