package wbif.sjx.ModularImageAnalysis.Object;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjCollectionTest {

    @Test
    public void testGetFirstPresent() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        ObjCollection collection = new ObjCollection("TestObj",false);

        Obj obj = new Obj("New obj",0,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("New obj",1,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        obj = new Obj("New obj",2,dppXY,dppZ,calibratedUnits);
        collection.add(obj);

        Obj firstObj = collection.getFirst();
        assertNotNull(firstObj);
        assertEquals(0,firstObj.getID());

    }

    @Test
    public void testGetFirstAbsent() {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        ObjCollection collection = new ObjCollection("TestObj",false);
        Obj firstObj = collection.getFirst();
        assertNull(firstObj);

    }

    @Test @Ignore
    public void testGetSpatialLimits() {

    }

    @Test @Ignore
    public void testGetTimepointLimits() {

    }

    @Test @Ignore
    public void testGetLargestID() {

    }

    @Test @Ignore
    public void testConvertObjectsToImageSingleColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageSingleColourNoTemplateImage() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageRandomColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageMeasurementColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageIDColour() {
    }

    @Test @Ignore
    public void testConvertObjectsToImageParentIDColour() {
    }

    @Test @Ignore
    public void testGetIDsIDScientific() {

    }

    @Test @Ignore
    public void testGetIDsIDZeroDecimalPlaces() {

    }

    @Test @Ignore
    public void testGetIDsIDOneDecimalPlaces() {

    }

    @Test @Ignore
    public void testGetIDsIDTwoDecimalPlaces() {

    }

    @Test @Ignore
    public void testGetIDsParentID() {

    }

    @Test @Ignore
    public void testGetIDsMeasurement() {

    }

    @Test @Ignore
    public void testGetHuesSingleColour() {

    }

    @Test @Ignore
    public void testGetHuesSingleColourNormalised() {

    }

    @Test @Ignore
    public void testGetHuesRandomColour() {

    }

    @Test @Ignore
    public void testGetHuesRandomColourNormalised() {

    }

    @Test @Ignore
    public void testGetHuesMeasurementColour() {

    }

    @Test @Ignore
    public void testGetHuesMeasurementColourNormalised() {

    }

    @Test @Ignore
    public void testGetHuesIDColour() {

    }

    @Test @Ignore
    public void testGetHuesIDColourNormalised() {

    }

    @Test @Ignore
    public void testGetHuesParentIDColour() {

    }

    @Test @Ignore
    public void testGetHuesParentIDColourNormalised() {

    }

    @Test @Ignore
    public void testGetColoursSingleColour() {

    }

    @Test @Ignore
    public void testGetColoursSingleColourNormalised() {

    }


    @Test @Ignore
    public void testGetColoursRandomColour() {

    }

    @Test @Ignore
    public void testGetColoursRandomColourNormalised() {

    }

    @Test @Ignore
    public void testGetColoursMeasurementColour() {

    }

    @Test @Ignore
    public void testGetColoursMeasurementColourNormalised() {

    }

    @Test @Ignore
    public void testGetColoursIDColour() {

    }

    @Test @Ignore
    public void testGetColoursIDColourNormalised() {

    }

    @Test @Ignore
    public void testGetColoursParentIDColour() {

    }

    @Test @Ignore
    public void testGetColoursParentIDColourNormalised() {

    }

    @Test @Ignore
    public void testGetByEquals() {

    }

}