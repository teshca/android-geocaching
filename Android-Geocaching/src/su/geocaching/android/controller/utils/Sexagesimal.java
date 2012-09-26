package su.geocaching.android.controller.utils;

//TODO: Rename, implement gettes and setters with argument validation
public class Sexagesimal {
    public int degrees;
    public double minutes;

    public Sexagesimal(Sexagesimal sexagesimal) {
        this.degrees = sexagesimal.degrees;
        this.minutes = sexagesimal.minutes;
    }

    public Sexagesimal(int degrees, double minutes) {
        //if (Math.abs(degrees) > 180 || minutes >= 60) {
        this.degrees = degrees;
        this.minutes = minutes;
    }

    public Sexagesimal(int coordinateE6) {
        degrees = coordinateE6 / 1000000;
        int minutesE6 = (Math.abs(coordinateE6) % 1000000) * 60;
        minutes = minutesE6 / 1E6;
    }

    public Sexagesimal roundTo(int i) {
        double precision = Math.pow(10, i);
        Sexagesimal copy = new Sexagesimal(this);
        copy.minutes = Math.round(minutes * precision);
        if (copy.minutes == 60 * precision) {
            copy.minutes = 0;
            if (copy.degrees < 0) copy.degrees--;
            else copy.degrees++;
        }
        copy.minutes /= precision;
        return copy;
    }

    public int toCoordinateE6() {
        double coordinateE6 = Math.signum(degrees) * (Math.abs(degrees) + (minutes / 60.0)) * 1E6;
        return (int) Math.round(coordinateE6);
    }
}