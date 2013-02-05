package su.geocaching.android.ui.test;

import junit.framework.TestCase;
import su.geocaching.android.controller.utils.Sexagesimal;

public class SexagesimalTest extends TestCase {

    public void testPositive() {
        Sexagesimal sexagesimal = new Sexagesimal(60.5);
        assertEquals(60, sexagesimal.degrees);
        assertEquals(30d, sexagesimal.minutes);
    }

    public void testNegative() {
        Sexagesimal sexagesimal = new Sexagesimal(-60.75);
        assertEquals(-60, sexagesimal.degrees);
        assertEquals(45d, sexagesimal.minutes);
    }

    public void testZero() {
        Sexagesimal sexagesimal = new Sexagesimal(0.25);
        assertEquals(0, sexagesimal.degrees);
        assertEquals(15d, sexagesimal.minutes);
    }

    public void testRoundTo() {
        Sexagesimal sexagesimal = new Sexagesimal(0, 34.33456);
        assertEquals(34.335, sexagesimal.roundTo(3).minutes);
    }

    public void testRoundToOverflow() {
        Sexagesimal sexagesimal = new Sexagesimal(-1, 59.999556);
        assertEquals(0d, sexagesimal.roundTo(3).minutes);
        assertEquals(-2, sexagesimal.roundTo(3).degrees);
    }
}
