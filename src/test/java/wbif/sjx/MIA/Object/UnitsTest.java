package wbif.sjx.MIA.Object;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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