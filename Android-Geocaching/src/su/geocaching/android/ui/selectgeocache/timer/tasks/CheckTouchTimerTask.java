package su.geocaching.android.ui.selectgeocache.timer.tasks;

import android.util.Log;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.timer.State;

import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @date 25.11.10 19:45
 */
public class CheckTouchTimerTask extends TimerTask{
    private SelectGeoCacheMap map;
    private State state;

    public CheckTouchTimerTask(State state, SelectGeoCacheMap map) {
        this.map = map;
        this.state = state;
    }

    @Override
    public void run() {
        Log.d("TASK", "touch start");
        if(map.touchHappened()) {
            Log.d("TASK", "touch detect");
            state.setTrue(3);
            map.setTouchHappened(false);
        }
    }
}
