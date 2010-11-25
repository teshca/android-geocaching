package su.geocaching.android.ui.selectgeocache.timer.tasks;

import android.util.Log;
import su.geocaching.android.ui.selectgeocache.timer.State;

import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @date 25.11.10 19:45
 */
public class CheckRequestTimerTask extends TimerTask {
    private State state;

    public CheckRequestTimerTask(State state) {
        this.state = state;
    }

    @Override
    public void run() {
        state.setTrue(1);
    }
}