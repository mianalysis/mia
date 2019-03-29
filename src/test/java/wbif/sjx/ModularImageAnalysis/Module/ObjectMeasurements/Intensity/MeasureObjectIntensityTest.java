// TODO: Add intensity distribution (centre of mass) test values
// TODO: Test 4D image stacks and objects

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.IJ;
import ij.ImagePlus;
import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Sphere3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 09/09/2017.
 */
public class MeasureObjectIntensityTest extends ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new MeasureObjectIntensity().getTitle());

    }

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectIntensity().getHelp());

    }

    @Test
    public void testRun8bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MEAN_8BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY // Test_image_MEAN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MIN_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MIN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MAX_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MAX").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_STD_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_STDEV").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_SUM_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_SUM").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

        }
    }

    @Test
    public void testRun16bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_16bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MEAN_16BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY // Test_image_MEAN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MIN_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MIN").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MAX_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MAX").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_STD_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_STDEV").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_SUM_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_SUM").getValue();
            assertEquals("Measurement value", expected, actual, 1E-2);

        }
    }

    @Test
    public void testRun32bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/NoisyGradient/NoisyGradient3D_32bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals("Number of ObjSets in Workspace",1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MEAN_32BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY // Test_image_MEAN").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MIN_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MIN").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MAX_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MAX").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_STD_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_STDEV").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_SUM_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_SUM").getValue();
            assertEquals("Measurement value", expected, actual,1E-2);

        }
    }

    /**
     * This tests the mean and stdev intensity distance on a single object (approximately spherical) against a shell
     * of intensity 2px inside the object.  This test doesn't take differences in XY and Z scaling into account.
     * @throws Exception
     */
    @Test
    public void testMeasureWeightedEdgeDistance2pxShellInside() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Sphere3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String imageName = "Test_image";
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureObjectIntensity/BinarySphere3D_1pxInside10pxOutsideShell_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image intensityImage = new Image(imageName,ipl);
        workspace.addImage(intensityImage);

        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,imageName);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,inputObjectsName);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.EDGE_DISTANCE_MODE,MeasureObjectIntensity.EdgeDistanceModes.INSIDE_ONLY);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Getting object (there is only one)
        Obj object = testObjects.values().iterator().next();
        String meanMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.MEAN_EDGE_DISTANCE_PX);
        String stdevMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.STD_EDGE_DISTANCE_PX);

        assertEquals(1,object.getMeasurement(meanMeasName).getValue(),0.5);
        assertEquals(0,object.getMeasurement(stdevMeasName).getValue(),0.5);

    }

    @Test
    public void testMeasureWeightedEdgeDistance10pxShellOutside() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Sphere3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String imageName = "Test_image";
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureObjectIntensity/BinarySphere3D_1pxInside10pxOutsideShell_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image intensityImage = new Image(imageName,ipl);
        workspace.addImage(intensityImage);

        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,imageName);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,inputObjectsName);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,true);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.EDGE_DISTANCE_MODE,MeasureObjectIntensity.EdgeDistanceModes.OUTSIDE_ONLY);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Getting object (there is only one)
        Obj object = testObjects.values().iterator().next();
        String meanMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.MEAN_EDGE_DISTANCE_PX);
        String stdevMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.STD_EDGE_DISTANCE_PX);

        assertEquals(10,object.getMeasurement(meanMeasName).getValue(),0.5);
        assertEquals(0,object.getMeasurement(stdevMeasName).getValue(),0.5);

    }
}