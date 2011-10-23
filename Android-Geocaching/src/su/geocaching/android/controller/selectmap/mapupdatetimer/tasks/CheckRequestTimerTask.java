package su.geocaching.android.controller.selectmap.mapupdatetimer.tasks;

import su.geocaching.android.controller.selectmap.mapupdatetimer.MapUpdateTimerState;

import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class CheckRequestTimerTask extends TimerTask {
    private final MapUpdateTimerState state;

    public CheckRequestTimerTask(MapUpdateTimerState state) {
        this.state = state;
    }

    @Override
    public void run() {
        state.setRequestSentTrue();
    }
}