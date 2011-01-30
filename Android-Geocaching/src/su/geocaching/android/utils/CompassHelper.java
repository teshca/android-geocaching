/*
 *  "Analog Compass" is an application for devices based on android os. 
 *  The application shows the orientation based on the intern magnetic sensor.   
 *  Copyright (C) 2009  Dieter Roth
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 3 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, see <http://www.gnu.org/licenses/>.
 */
package su.geocaching.android.utils;

/**
 * @author Nikita Bumakov
 */
public class CompassHelper {

	public static float calculateNormalDifference(float lastDirection, float curSetPoint) {
		float diff = curSetPoint - lastDirection;
		return CompassHelper.normalizeAngle(diff);
	}

	public static float normalizeAngle(float diff) {
		while (diff < -180) {
			diff += 360;
		}
		while (diff > 180) {
			diff -= 360;
		}
		return diff;
	}
}
