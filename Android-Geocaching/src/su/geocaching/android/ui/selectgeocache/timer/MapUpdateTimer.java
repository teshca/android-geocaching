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

    public MapUpdateTimer(SelectGeoCacheMap map) {
        State state = new State(map);

        this.schedule(new CheckRequestTimerTask(state), 100, 2000);
        this.schedule(new CheckTouchTimerTask(state, map), 0, 500);
        this.schedule(new CheckMapStateTimerTask(state, map, map.getCenter(), map.getZoom()), 0, 700);
    }
}
