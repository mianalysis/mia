package wbif.sjx.MIA.Object;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnitsTest {

    @Test
    public void testReplace() {
        Units.setUnits(Units.SpatialUnits.NANOMETRE);

        String testString = "Test string ${CAL} with units";
        String actual = Units.replace(testString);
        String expected = "Test string nm with units";

        assertEquals(expected,actual);

    }
}