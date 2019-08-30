package wbif.sjx.MIA.Module.ImageMeasurements;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen on 17/11/2017.
 */
public class MeasureIntensityDistributionTest extends ModuleTest {

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureIntensityDistribution(null).getDescription());
    }

    @Test @Disabled
    public void testMeasureIntensityWeightedProximity() throws Exception {

    }

    @Test @Disabled
    public void run() throws Exception {

    }
}