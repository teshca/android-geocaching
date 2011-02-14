package su.geocaching.android.utils;

public class CompassHelper {

	public static String degreesToString(float direction) {
		direction = normalizeAngle(direction);
		if (direction < 0){
			direction = 360+direction;
		}
		return String.format("%.1f°", direction);
	}

	public static float calculateNormalDifference(float lastDirection, float currentDirection) {
		float difference = currentDirection - lastDirection;
		return normalizeAngle(difference);
	}

	public static float normalizeAngle(float angle) {
		angle %= 360;
		if (angle < -180) {
			angle += 360;
		}
		if (angle > 180){
			angle -= 360;}
		return angle;
	}	
}
