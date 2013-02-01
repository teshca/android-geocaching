package su.geocaching.android.controller.utils;

//TODO: Rename, implement gettes and setters with argument validation
//TODO: Implement unit tests
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

    public Sexagesimal(double coordinate) {
        minutes = (Math.abs(coordinate) % 1) * 60;
        degrees = (int) Math.abs(coordinate);
        if (coordinate < 0) degrees = -degrees;
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

    public double toCoordinate() {
        double coordinate = Math.abs(degrees) + (minutes / 60.0);
        if (degrees < 0) coordinate = -coordinate;
        return coordinate;
    }

    @Deprecated
    public int toCoordinateE6() {
        return (int) (toCoordinate() * 1E6);
    }
}