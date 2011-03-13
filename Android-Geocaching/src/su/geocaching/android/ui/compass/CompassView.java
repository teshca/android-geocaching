package su.geocaching.android.ui.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.compass.CompassDrawningHelper;
import su.geocaching.android.controller.compass.ICompassAnimation;
import su.geocaching.android.controller.compass.StandartCompassDrawning;
import su.geocaching.android.controller.compass.WhiteStandartCompassDrawning;

/**
 * View which displays compass contains of bitmaps for searching geocache.
 * 
 * @author Nikita Bumakov
 */
public class CompassView extends SurfaceView implements SurfaceHolder.Callback, ICompassAnimation {

    private static final String TAG = CompassView.class.getCanonicalName();

    private CompassDrawningHelper helper;
    private Context context;

    private float northDirection; // in degrees
    private float cacheDirection;
    private boolean ready = false;
    private boolean isLocationFixed = false;

    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogManager.d(TAG, "new CompassView");

        helper = new StandartCompassDrawning(context);
        ready = true; // Is it need?
        this.context = context;

        setMinimumWidth(240);
        setMinimumHeight(240);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (ready) {
            super.onDraw(canvas);
            helper.draw(canvas, northDirection);
            if (isLocationFixed) {
                helper.drawCacheArrow(canvas, cacheDirection + northDirection);
            }
        } else {
            LogManager.w("draw", "not ready");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogManager.d(TAG, "onSizeChanged" + w + " " + h);
        helper.onSizeChanged(w, h);
    }

    @Override
    public boolean setDirection(float direction) {
        northDirection = -direction;
        return doAnim();
    }

    private boolean doAnim() {
        boolean success = false;
        Canvas c = null;
        SurfaceHolder holder = this.getHolder();
        if (holder != null) {
            try {
                c = holder.lockCanvas(null);
                synchronized (holder) {
                    if (c != null) {
                        success = onDrawnCheck(c);
                    }
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }
        }
        return success;
    }

    private boolean onDrawnCheck(Canvas canvas) {
        if (ready) {
            onDraw(canvas);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param direction
     *            - direction to geocache in degrees
     */
    public void setCacheDirection(float direction) {
        LogManager.d(TAG, "Compass View - setDirectionToGeoCache ");
        cacheDirection = direction;
        isLocationFixed = true;
        doAnim();
    }

    public void setLocationFix(boolean isLocationFix) {
        this.isLocationFixed = isLocationFix;
    }

    /**
     * @return the helper
     */
    public CompassDrawningHelper getHelper() {
        return helper;
    }

    /**
     * @param helper
     *            the helper to set
     */
    // TODO too many objects
    public void setHelper(String string) {
        if (string.equals("CLASSIC")) {
            helper = new StandartCompassDrawning(context);
        } else if (string.equals("PALE")) {
            helper = new WhiteStandartCompassDrawning(context);
        }
        if (getWidth() > 0) {
            helper.onSizeChanged(getWidth(), getHeight());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogManager.d(TAG, "CompassView - surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        ready = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        ready = false;
    }
}
