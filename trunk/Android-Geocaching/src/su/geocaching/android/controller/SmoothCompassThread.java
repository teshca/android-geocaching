package su.geocaching.android.controller;

import su.geocaching.android.ui.searchgeocache.CompassView;
import su.geocaching.android.utils.CompassHelper;
import su.geocaching.android.utils.log.LogHelper;
import android.content.Context;

/**
 * @author Nikita Bumakov
 */
public class SmoothCompassThread extends Thread implements ICompassAware {

	private static final String TAG = SmoothCompassThread.class.getCanonicalName();

	private static final float ARRIVED_EPS = 0.7f;
	private static final float LEAVED_EPS = 2.5f;
	private static final float SPEED_EPS = 0.5f;

	public static final int LONG_SLEEP = 100;
	public static final int STANDART_SLEEP = 15;
	private float currentDirection = 0;
	private float avgDirection = 0;
	private CompassManager compassManager;
	private CompassView compassView;
	private boolean isRunning = false;
	private boolean isFinished = false;

	public boolean isFinished() {
		return isFinished;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public SmoothCompassThread(CompassView view, Context context) {
		LogHelper.d(TAG, "new SmoothCompassThread");
		compassManager = Controller.getInstance().getCompassManager(context);
		compassManager.addObserver(this);
		this.compassView = view;
	}

	@Override
	public void run() {
		LogHelper.d(TAG, "Thread - run");
		isFinished = false;
		float speed = 0;
		float lastDirection = 0;
		boolean forcePaint = true;
		boolean isArrived = false; // The needle has not arrived the goalDirection

		while (isRunning) {
			float currrentSetPoint = currentDirection;
			boolean needPainting = this.isNeedPainting(isArrived, speed, lastDirection, currrentSetPoint) || forcePaint;

			if (needPainting) {
				isArrived = false;
				float diff = CompassHelper.calculateNormalDiff(lastDirection, currrentSetPoint);
				speed = calculateSpeed(diff, speed);
				currrentSetPoint = lastDirection + speed;
				lastDirection = currrentSetPoint;

				boolean successPaint = compassView.setDirection(currrentSetPoint);
				forcePaint = !successPaint;

			} else {
				isArrived = true;
			}

			// Wait for next drawing
			try {
				if (isArrived) {
					try {
						Thread.sleep(LONG_SLEEP);
					} catch (InterruptedException e) {
						LogHelper.w(TAG, "Unexpected long sleep", e);
					}
				} else {
					try {
						Thread.sleep(STANDART_SLEEP);
					} catch (InterruptedException e) {
						LogHelper.w(TAG, "Unexpected sleep", e);
					}

				}
			} catch (Throwable e) {
				forcePaint = true;
				LogHelper.w(this.getClass().getName(), "Unexpected", e);
			}
		}
	}

	@Override
	public void updateBearing(float bearing) {
		float newDirection = bearing;
		float diff = newDirection - avgDirection;
		diff = CompassHelper.normalizeAngle(diff);
		if (Math.abs(diff) < 5) {
			newDirection = avgDirection + diff / 4;
		} else {
			// setSensorListenerState(SensorListenerState.ACTION);
		}
		newDirection = CompassHelper.normalizeAngle(newDirection);
		avgDirection = newDirection;
		currentDirection = avgDirection;
	}

	private float calculateSpeed(float diff, float oldSpeed) {
		oldSpeed = oldSpeed * 0.75f; // friction
		oldSpeed += diff / 40.0f; // acceleration
		return oldSpeed;
	}

	private boolean isNeedPainting(boolean pArrived, float pSpeed, float curNeedleDirection, float setPoint) {
		if (pArrived) {
			if (Math.abs(curNeedleDirection - setPoint) > LEAVED_EPS) {
				return (true);
			}
			return (false);
		} else {
			if (Math.abs(curNeedleDirection - setPoint) < ARRIVED_EPS && Math.abs(pSpeed) < SPEED_EPS) {
				return (false);
			}
			return (true);
		}
	}

}
