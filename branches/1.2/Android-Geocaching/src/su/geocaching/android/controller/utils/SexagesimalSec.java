package su.geocaching.android.controller.utils;

//TODO: Rename, implement gettes and setters with argument validation
public class SexagesimalSec {
    public int degrees;
    public int minutes;
    public double seconds;

    public SexagesimalSec(SexagesimalSec sexagesimalSec) {
        this.degrees = sexagesimalSec.degrees;
        this.minutes = sexagesimalSec.minutes;
        this.seconds = sexagesimalSec.seconds;
    }

    public SexagesimalSec(int degrees, int minutes, double seconds) {
        //            if (Math.abs(degrees) > 180 || minutes >= 60 || seconds >= 60) {
        this.degrees = degrees;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public SexagesimalSec(int coordinateE6) {
        degrees = coordinateE6 / 1000000;
        int minutesE6 = (coordinateE6 % 1000000) * 60;
        minutes = minutesE6 / 1000000;
        int secondsE6 = (minutesE6 % 1000000) * 60;
        seconds = secondsE6 / 1E6;
    }

    public SexagesimalSec roundTo(int i) {
        double precision = Math.pow(10, i);
        SexagesimalSec copy = new SexagesimalSec(this);
        copy.seconds = Math.round(seconds * precision);
        if (copy.seconds == 60 * precision) {
            copy.seconds = 0;
            copy.minutes++;
        }
        copy.seconds /= precision;
        if (copy.minutes == 60) {
            copy.minutes = 0;
            if (copy.degrees < 0) {
                copy.degrees--;
            } else {
                copy.degrees++;
            }
        }
        return copy;
    }

    public int toCoordinateE6() {
        double coordinateE6 = (degrees + (minutes / 60.0) + (seconds / 3600.0)) * 1E6;
        return (int) Math.round(coordinateE6);
    }
}