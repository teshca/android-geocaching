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

    public SexagesimalSec(double coordinate) {
        double min = (Math.abs(coordinate) % 1) * 60;
        degrees = (int) Math.abs(coordinate);
        minutes = (int) min;
        seconds = (min % 1) * 60;
        if (coordinate < 0) degrees = -degrees;
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

    public double toCoordinate() {
        double coordinate = (Math.abs(degrees) + (minutes / 60.0) + (seconds / 3600.0));
        if (degrees < 0) coordinate = -coordinate;
        return coordinate;
    }
}