package su.geocaching.android.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author: Grigory Kalabin
 * @since: 24.10.2011
 */
public class ProgressBarView extends View {

    private AnimationDrawable animation;
    private boolean isAnimationRunning;

    /**
     * {@inheritDoc}
     */
    public ProgressBarView(Context context) {
        super(context);
        initAnimation();
    }

    /**
     * {@inheritDoc}
     */
    public ProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnimation();
    }

    /**
     * {@inheritDoc}
     */
    public ProgressBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAnimation();
    }

    private void initAnimation() {
        setBackgroundResource(R.anim.earth_anim);
        animation = (AnimationDrawable) getBackground();
        isAnimationRunning = false;
    }

    /**
     * Start animation of progress bar. If animation already running - do nothing.
     */
    public void startAnimation() {
        if (!isAnimationRunning) {
            animation.start();
            isAnimationRunning = true;
        }
    }

    /**
     * Stop animation of progress bar. If animation already stopped - do nothing.
     */
    public void stopAnimation() {
        if (isAnimationRunning) {
            animation.stop();
            isAnimationRunning = false;
        }
    }
}
