package wbif.sjx.MIA.Object;

import org.junit.jupiter.api.Test;

import wbif.sjx.MIA.Object.Units.SpatialUnit;
import wbif.sjx.MIA.Object.Units.TemporalUnit;

import static org.junit.jupiter.api.Assertions.*;

public class UnitsTest {

    @Test
    public void testReplace() {
        SpatialUnit.setUnit(SpatialUnit.AvailableUnits.NANOMETRE);

        String testString = "Test string ${SCAL} with units";
        testString = SpatialUnit.replace(testString);
        String actual = TemporalUnit.replace(testString);
        String expected = "Test string nm with units";

        assertEquals(expected,actual);

    }
}