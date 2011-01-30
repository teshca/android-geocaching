package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.controller.compass.CompassDrawningHelper;
import su.geocaching.android.controller.compass.ICompassAnimation;
import su.geocaching.android.controller.compass.StandartCompassDrawning;
import su.geocaching.android.utils.log.LogHelper;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * View which displays compass contains of bitmaps for searching geocache.
 * 
 * @author Nikita Bumakov
 */
public class CompassView extends SurfaceView implements SurfaceHolder.Callback, ICompassAnimation {

	private static final String TAG = SurfaceView.class.getCanonicalName();

	private CompassDrawningHelper helper;
	private float bearingToNorth; // in degrees
	private float absoluteBearingToCache, relativeBearingToCache; // in degrees
	private boolean ready = false;
	private boolean isLocationFixed = false;

	public CompassView(Context context) {
		this(context, null);
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		helper = new StandartCompassDrawning(context);
		init();
	}

	private void init() {
		LogHelper.d(TAG, "new CompassView");
		
		setMinimumWidth(240);
		setMinimumHeight(240);
		bearingToNorth = relativeBearingToCache = absoluteBearingToCache = 0;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (ready) {
			super.onDraw(canvas);
			helper.draw(canvas, bearingToNorth, relativeBearingToCache);
			if (isLocationFixed) {
				// drawGeoCache(canvas);
			}
		} else {
			LogHelper.w("draw", "not ready");
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		LogHelper.d(TAG, "onSizeChanged" + w + " " + h);
		helper.onSizeChanged(w, h);
		ready = true;
	}
	
	// private void drawGeoCache(Canvas canvas) {
	// double bearingRad = (double) (relativeBearingToCache * Math.PI) / 180;
	// int cx = (int) ((getWidth() - cacheBitmap.getWidth()) / 2 + Math.sin(bearingRad) * compassRadius * 0.9);
	// int cy = (int) ((getHeight() - cacheBitmap.getHeight()) / 2 - Math.cos(bearingRad) * compassRadius * 0.9);
	// canvas.drawBitmap(cacheBitmap, cx, cy, paint);
	// }

	@Override
	public boolean setDirection(float direction) {
		bearingToNorth = -direction;
		relativeBearingToCache = absoluteBearingToCache + bearingToNorth;
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
	public void setDirectionToGeoCache(float direction) {
		this.absoluteBearingToCache = (int) direction;
		relativeBearingToCache = absoluteBearingToCache + bearingToNorth;
		// invalidate();
	}

	public void setLocationFix(boolean isLocationFix) {
		this.isLocationFixed = isLocationFix;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "CompassView - surfaceChanged");
		// helper.onSizeChanged(width, height);
		// ready = true;
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
