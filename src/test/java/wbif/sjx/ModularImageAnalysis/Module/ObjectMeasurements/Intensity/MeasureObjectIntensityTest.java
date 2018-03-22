// TODO: Add intensity distribution (centre of mass) test values
// TODO: Test 4D image stacks and objects

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity.MeasureObjectIntensity;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 09/09/2017.
 */
public class MeasureObjectIntensityTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new MeasureObjectIntensity().getTitle());

    }

    @Test
    public void testRun8bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MEAN,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MIN,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MAX,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_STDEV,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_SUM,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.run(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MEAN_8BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY//Test_image_MEAN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MIN_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_MIN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MAX_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_MAX").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_STD_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_STDEV").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_SUM_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_SUM").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

        }
    }

    @Test
    public void testRun16bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MEAN,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MIN,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MAX,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_STDEV,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_SUM,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.run(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MEAN_16BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY//Test_image_MEAN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MIN_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_MIN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MAX_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_MAX").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_STD_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_STDEV").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_SUM_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_SUM").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

        }
    }

    @Test
    public void testRun32bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MEAN,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MIN,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_MAX,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_STDEV,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_SUM,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.run(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MEAN_32BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY//Test_image_MEAN").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MIN_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_MIN").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_MAX_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_MAX").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_STD_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_STDEV").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_I_SUM_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY//Test_image_SUM").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

        }
    }

}