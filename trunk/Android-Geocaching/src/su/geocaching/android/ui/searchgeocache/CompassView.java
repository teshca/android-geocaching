package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.utils.log.LogHelper;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * View which displays compass contains of bitmaps for searching geocache.
 * 
 * @author Nikita Bumakov
 */
public class CompassView extends SurfaceView implements SurfaceHolder.Callback {

	//private static final String TAG = SurfaceView.class.getCanonicalName();	

	private float bearingToNorth; // in degrees
	private float absoluteBearingToCache, relativeBearingToCache; // in degrees
	//private boolean isLocationFixed = false;
	boolean ready = true;	
	
	private Paint paint;

	public CompassView(Context context) {
		this(context, null);
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initParameters();
	}

	private void initParameters() {
		bearingToNorth = 0;
		relativeBearingToCache = absoluteBearingToCache = 0;	
		paint = new Paint();
		paint.setAntiAlias(true);		
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (ready) {
			super.onDraw(canvas);
			canvas.drawColor(Color.WHITE);
			drawArrow(canvas);
//			if (isLocationFixed) {
//				drawGeoCache(canvas);
//			}
		} else {
			LogHelper.w("draw", "not ready");
		}
	}

	private void drawArrow(Canvas canvas) {
		float x = getWidth() / 2;
		float y = getHeight() / 2;
		float radius = Math.min(x, y);
		double bearingRad = (double) (relativeBearingToCache * Math.PI) / 180;
		float x2 = (float) (x + Math.sin(bearingRad) * radius);
		float y2 = (float) (y - Math.cos(bearingRad) * radius);
		paint.setColor(Color.BLUE);
		canvas.drawLine(x, y, x2, y2, paint);
		paint.setColor(Color.RED);
		x2 = (float) (x - Math.sin(bearingRad) * radius);
		y2 = (float) (y + Math.cos(bearingRad) * radius);
		canvas.drawLine(x, y, x2, y2, paint);

	}

//	private void drawGeoCache(Canvas canvas) {
//		double bearingRad = (double) (relativeBearingToCache * Math.PI) / 180;
//		int cx = (int) ((getWidth() - cacheBitmap.getWidth()) / 2 + Math.sin(bearingRad) * compassRadius * 0.9);
//		int cy = (int) ((getHeight() - cacheBitmap.getHeight()) / 2 - Math.cos(bearingRad) * compassRadius * 0.9);
//		canvas.drawBitmap(cacheBitmap, cx, cy, paint);
//	}

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
	 * @param bearingToGeoCache
	 *            - bearing to geocache in degrees
	 */
	public void setBearingToGeoCache(float bearingToGeoCache) {
		this.absoluteBearingToCache = (int) bearingToGeoCache;
		relativeBearingToCache = absoluteBearingToCache + bearingToNorth;
		invalidate();
	}

	public void setLocationFix(boolean isLocationFix) {
		//this.isLocationFixed = isLocationFix;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LogHelper.d("TESTTT", "surfaceCreated");
		ready = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		ready = false;
	}

}
