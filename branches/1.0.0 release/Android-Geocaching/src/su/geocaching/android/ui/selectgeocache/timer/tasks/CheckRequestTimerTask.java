package su.geocaching.android.ui.selectgeocache.timer.tasks;

import su.geocaching.android.ui.selectgeocache.timer.State;

import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class CheckRequestTimerTask extends TimerTask {
    private final State state;

    public CheckRequestTimerTask(State state) {
        this.state = state;
    }

    @Override
    public void run() {
        state.setRequestSentTrue();
    }
}