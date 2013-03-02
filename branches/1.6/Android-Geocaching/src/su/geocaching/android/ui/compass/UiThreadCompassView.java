package su.geocaching.android.ui.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.compass.*;
import su.geocaching.android.controller.managers.IBearingAware;
import su.geocaching.android.controller.managers.LogManager;

/**
 * @author Nikita Bumakov
 */
public class UiThreadCompassView extends View implements IBearingAware {

    private static final String TAG = UiThreadCompassView.class.getCanonicalName();
    private static final long MIN_INVALIDATE_TIME = 100; //in ms

    private AbstractCompassDrawing compassDrawing;

    private float northDirection; // in degrees
    private Float cacheDirection;
    private CompassSourceType sourceType;

    public UiThreadCompassView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LogManager.d(TAG, "new UiThreadCompassView");
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (compassDrawing != null) {
            compassDrawing.draw(canvas, northDirection);
            if (cacheDirection != null) {
                compassDrawing.drawCacheArrow(canvas, cacheDirection + northDirection);
            }
        } else {
            LogManager.w(TAG, "draw not ready");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogManager.d(TAG, "onSizeChanged" + w + " " + h);
        compassDrawing.onSizeChanged(w, h);
    }

    /**
     * @param direction
     *         - direction to geocache in degrees
     */
    public void setCacheDirection(float direction) {
        cacheDirection = direction;
    }

    /**
     * @param helperType
     *         //TODO describe it
     */
    public void setHelper(String helperType) {
        if (compassDrawing != null && helperType.equals(compassDrawing.getType())) {
            return;
        }

        if (helperType.equals(AbstractCompassDrawing.TYPE_CLASSIC)) {
            compassDrawing = new DefaultCompassDrawing();
        } else if (helperType.equals(AbstractCompassDrawing.TYPE_PALE)) {
            compassDrawing = new PaleStandardCompassDrawing();
        } else if (helperType.equals(AbstractCompassDrawing.TYPE_PREVIEW)) {
            compassDrawing = new PreviewCompassDrawing();
        }

        if (getWidth() > 0) {
            compassDrawing.onSizeChanged(getWidth(), getHeight());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        LogManager.d(TAG, "onAttachedToWindow");
        if (getVisibility() == VISIBLE) {
            Controller.getInstance().getCompassManager().addSubscriber(this);
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        LogManager.d(TAG, "onDetachedFromWindow");
        Controller.getInstance().getCompassManager().removeSubscriber(this);
        super.onDetachedFromWindow();
    }

    private long time = 0;

    @Override
    //TODO: check this method
    public void updateBearing(float bearing, float declination, CompassSourceType sourceType) {
        this.sourceType = sourceType;
        long newTime = System.currentTimeMillis();
        if (newTime - time > MIN_INVALIDATE_TIME) {
            northDirection = -bearing;
            invalidate();
            time = newTime;
        }
    }
}
