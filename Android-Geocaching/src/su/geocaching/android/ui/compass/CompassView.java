package su.geocaching.android.ui.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import su.geocaching.android.controller.compass.*;
import su.geocaching.android.controller.managers.LogManager;

/**
 * View which displays compass contains of bitmaps for searching geocache.
 *
 * @author Nikita Bumakov
 */
public class CompassView extends SurfaceView implements SurfaceHolder.Callback, ICompassAnimation {

    private static final String TAG = CompassView.class.getCanonicalName();

    private AbstractCompassDrawing compassDrawing;

    private float northDirection; // in degrees
    private float cacheDirection;
    private boolean ready = false;
    private boolean isLocationFixed = false;

    public CompassView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LogManager.d(TAG, "new CompassView");

        //compassDrawing = new DefaultCompassDrawing();
        ready = true; // Is it need?
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (ready && compassDrawing != null) {
            compassDrawing.draw(canvas, northDirection);
            if (isLocationFixed) {
                compassDrawing.drawCacheArrow(canvas, cacheDirection + northDirection);
            }
        } else {
            LogManager.w("TAG", "draw not ready");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogManager.d(TAG, "onSizeChanged" + w + " " + h);
        compassDrawing.onSizeChanged(w, h);
    }

    @Override
    public boolean setDirection(float direction) {
        northDirection = -direction;
        return doAnimation();
    }

    private boolean doAnimation() {
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
     * @param direction - direction to geocache in degrees
     */
    public void setCacheDirection(float direction) {
        cacheDirection = direction;
        isLocationFixed = true;
        //doAnimation();
    }

    public void setDistance(float distance) {
        compassDrawing.setDistance(distance);
    }

    public void setLocationFix(boolean isLocationFix) {
        this.isLocationFixed = isLocationFix;
    }

    /**
     * @return the helper
     */
    public AbstractCompassDrawing getHelper() {
        return compassDrawing;
    }

    /**
     * @param string //TODO describe it
     */
    // TODO too many objects
    public void setHelper(String string) {
        if (string.equals("CLASSIC") && !(compassDrawing instanceof DefaultCompassDrawing)) {
            compassDrawing = new DefaultCompassDrawing();
        } else if (string.equals("PALE") && !(compassDrawing instanceof WhiteStandardCompassDrawing)) {
            compassDrawing = new WhiteStandardCompassDrawing();
        } else if (string.equals("PREVIEW") && !(compassDrawing instanceof PreviewCompassDrawing)) {
            compassDrawing = new PreviewCompassDrawing();
        }

        if (getWidth() > 0) {
            compassDrawing.onSizeChanged(getWidth(), getHeight());
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
