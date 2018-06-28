package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import static org.junit.Assert.*;

/**
 * Created by Stephen on 17/11/2017.
 */
public class MeasureIntensityDistributionTest {

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new MeasureIntensityDistribution().getTitle());
    }

    @Test @Ignore
    public void testMeasureIntensityWeightedProximity() throws Exception {

    }

    @Test @Ignore
    public void run() throws Exception {

    }
}