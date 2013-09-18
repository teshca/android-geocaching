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

    /**
     * {@inheritDoc}
     */
    public ProgressBarView(Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    public ProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    public ProgressBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        animation = (AnimationDrawable) getBackground();
    }

    @Override
    protected void onAttachedToWindow() {
        animation.start();
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(INVISIBLE);
    }
}
