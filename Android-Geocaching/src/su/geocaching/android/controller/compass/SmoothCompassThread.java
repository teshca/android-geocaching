package su.geocaching.android.controller.compass;

import android.content.Context;
import su.geocaching.android.controller.CompassManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.ICompassAware;
import su.geocaching.android.controller.LogManager;

/**
 * The class provides a smooth rotation of compass
 * 
 * @author Nikita Bumakov
 */
public class SmoothCompassThread extends Thread implements ICompassAware {

    private static final String TAG = SmoothCompassThread.class.getCanonicalName();

    public static final int LONG_SLEEP = 100;
    public static final int STANDART_SLEEP = 20;

    private static final float ARRIVED_EPS = 0.65f;
    private static final float LEAVED_EPS = 2.5f;
    private static final float SPEED_EPS = 0.55f;

    private CompassSpeed speed;

    /**
     * @param speed - Speed mode of compass needle
     */
    public void setSpeed(CompassSpeed speed) {
        this.speed = speed;
    }

    private ICompassAnimation compassView;
    private CompassManager compassManager;

    private float goalDirection = 0;
    private boolean isRunning = false;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
        if (!isRunning) {
            LogManager.d(TAG, "SmoothCompassThread - isRunning false");
            compassManager.removeSubscriber(this);
        }
    }

    public SmoothCompassThread(ICompassAnimation compassView, Context context) {
        LogManager.d(TAG, "new SmoothCompassThread");
        compassManager = Controller.getInstance().getCompassManager(context.getApplicationContext());
        compassManager.addSubscriber(this);
        this.compassView = compassView;
        speed = CompassSpeed.NORMAL;
    }

    @Override
    public void run() {
        LogManager.d(TAG, "SmoothCompassThread - run");
        float speed = 0;
        float needleDirection = 0;
        boolean forcePaint = true;
        boolean isArrived = false; // The needle has not arrived the goalDirection

        while (isRunning) {
            float currentDirection = goalDirection;
            boolean needPainting = isNeedPainting(isArrived, speed, needleDirection, currentDirection) || forcePaint;

            if (needPainting) {
                isArrived = false;
                float difference = CompassHelper.calculateNormalDifference(needleDirection, currentDirection);
                speed = calculateSpeed(difference, speed);
                currentDirection = needleDirection + speed;
                needleDirection = currentDirection;
                forcePaint = !compassView.setDirection(needleDirection);
            } else {
                isArrived = true;
            }
            try {
                if (isArrived) {
                    Thread.sleep(LONG_SLEEP);
                } else {
                    Thread.sleep(STANDART_SLEEP);
                }
            } catch (InterruptedException e) {
                LogManager.w(TAG, "interrupt() was called for SmoothCompassThread while it was sleeping", e);
            }
        }
    }

    private float averageDirection = 0;

    @Override
    public void updateBearing(float bearing) {
        float newDirection = bearing;
        float difference = newDirection - averageDirection;
        difference = CompassHelper.normalizeAngle(difference);

        newDirection = averageDirection + difference / 4; // TODO extract constant

        newDirection = CompassHelper.normalizeAngle(newDirection);
        averageDirection = newDirection;
        goalDirection = averageDirection;
    }

    private float calculateSpeed(float difference, float oldSpeed) {
        switch (speed) {
            case DIRET:
                oldSpeed = oldSpeed * 0f; // friction
                oldSpeed += difference; // acceleration
                break;
            case SLOW:
                oldSpeed = oldSpeed * 0.75f;
                oldSpeed += difference / 40.0f;
                break;
            case NORMAL:
                oldSpeed = oldSpeed * 0.75f;
                oldSpeed += difference / 40.0f;
                break;
            case FAST:
                oldSpeed = oldSpeed * 0.75f;
                oldSpeed += difference / 8.0f;
                break;
            case SWING:
                oldSpeed = oldSpeed * 0.97f;
                oldSpeed += difference / 10.0f;
                break;
        }
        return oldSpeed;
    }

    private boolean isNeedPainting(boolean isArrived, float speed, float needleDirection, float goalDirection) {
        if (isArrived) {
            return Math.abs(needleDirection - goalDirection) > LEAVED_EPS;
        } else {
            return Math.abs(needleDirection - goalDirection) > ARRIVED_EPS || Math.abs(speed) > SPEED_EPS;
        }
    }
}
