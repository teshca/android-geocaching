package su.geocaching.android.controller.compass;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.CompassManager;
import su.geocaching.android.controller.managers.IBearingAware;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * The class provides a smooth rotation of compass
 *
 * @author Nikita Bumakov
 */
public class SmoothCompassThread extends Thread implements IBearingAware {

    private static final String TAG = SmoothCompassThread.class.getCanonicalName();

    private static final int LONG_SLEEP = 120;
    private static final int DEFAULT_SLEEP = 40;

    private static final float ARRIVED_EPS = 0.65f;
    private static final float LEAVED_EPS = 2.5f;
    private static final float SPEED_EPS = 0.55f;

    private CompassSpeed speed;
    private List<ICompassView> compassView = new LinkedList<ICompassView>();
    private CompassManager compassManager;

    private float goalDirection = 0;
    private boolean isRunning = false;

    public SmoothCompassThread(ICompassView... compassView) {
        LogManager.d(TAG, "new SmoothCompassThread");
        compassManager = Controller.getInstance().getCompassManager();
        compassManager.addSubscriber(this);


        if (compassView != null) {
            for (ICompassView compass : compassView) {
                this.compassView.add(compass);
            }
        }

        speed = CompassSpeed.NORMAL;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
        if (!isRunning) {
            LogManager.d(TAG, "SmoothCompassThread - isRunning false");
            compassManager.removeSubscriber(this);
        }
    }

    /**
     * @param speed - Speed mode of compass needle
     */
    public void setSpeed(CompassSpeed speed) {
        this.speed = speed;
    }

    public void addSubscriber(ICompassView compassView) {
        synchronized (this.compassView) {
            if (!this.compassView.contains(compassView)) {
                this.compassView.add(compassView);
            }
        }
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

                synchronized (compassView) {
                    if (compassView != null)
                        for (ICompassView compass : compassView) {
                            forcePaint |= !compass.setDirection(needleDirection);
                        }
                }
            } else {
                isArrived = true;
            }
            try {
                if (isArrived) {
                    Thread.sleep(LONG_SLEEP);
                } else {
                    Thread.sleep(DEFAULT_SLEEP);
                }
            } catch (InterruptedException e) {
                LogManager.w(TAG, "interrupt() was called for SmoothCompassThread while it was sleeping", e);
            }
        }
    }

    private float averageDirection = 0;

    @Override
    public void updateBearing(float bearing, CompassSourceType sourceType) {
        // update source type
        synchronized (compassView) {
            if (compassView != null)
                for (ICompassView compass : compassView) {
                    compass.setSourceType(sourceType);
                }
        }
        // update bearing
        float newDirection = bearing;
        float difference = newDirection - averageDirection;
        difference = CompassHelper.normalizeAngle(difference);

        newDirection = averageDirection + difference / 4; // TODO extract constant

        newDirection = CompassHelper.normalizeAngle(newDirection);
        goalDirection = averageDirection = newDirection;
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
                oldSpeed += difference / 25.0f;
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
