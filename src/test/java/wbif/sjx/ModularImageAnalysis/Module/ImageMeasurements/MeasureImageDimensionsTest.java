package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.TrackObjects;

import static org.junit.Assert.*;

public class MeasureImageDimensionsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureImageDimensions().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureImageDimensions().getHelp());
    }
}