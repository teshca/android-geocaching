package su.geocaching.android.ui.selectgeocache.timer.tasks;

import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.timer.State;

import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @date 25.11.10 19:45
 */
public class CheckTouchTimerTask extends TimerTask {
    private SelectGeoCacheMap map;
    private State state;

    public CheckTouchTimerTask(State state, SelectGeoCacheMap map) {
        this.map = map;
        this.state = state;
    }

    @Override
    public void run() {
        if (!map.touchHappened()) {
            state.setTouchedTrue();
        } else {
            map.setTouchHappened(false);
        }
    }
}
