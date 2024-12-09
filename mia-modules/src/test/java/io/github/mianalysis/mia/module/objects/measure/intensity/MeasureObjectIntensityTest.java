// TODO: Add intensity distribution (centre of mass) test values
// TODO: Test 4D image stacks and objects

package io.github.mianalysis.mia.module.objects.measure.intensity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;

/**
 * Created by Stephen Cross on 09/09/2017.
 */

public class MeasureObjectIntensityTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new MeasureObjectIntensity(null).getDescription());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRun8bit3D(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        Objs testObjects = new Objects3D(volumeType).getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_8bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity(null);
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        // measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals(1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjects(inputObjectsName));
        assertEquals(8,workspace.getObjects(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MEAN_8BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY // Test_image_MEAN").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MIN_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MIN").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MAX_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MAX").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_STD_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_STDEV").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_SUM_8BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_SUM").getValue();
            assertEquals(expected, actual, 1E-2);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRun16bit3D(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        Objs testObjects = new Objects3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_16bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity(null);
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        // measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals(1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjects(inputObjectsName));
        assertEquals(8,workspace.getObjects(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MEAN_16BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY // Test_image_MEAN").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MIN_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MIN").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MAX_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MAX").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_STD_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_STDEV").getValue();
            assertEquals(expected, actual, 1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_SUM_16BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_SUM").getValue();
            assertEquals(expected, actual, 1E-2);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRun32bit3D(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        Objs testObjects = new Objects3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/noisygradient/NoisyGradient3D_32bit.zip").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image",ipl);
        workspace.addImage(image);

        // Initialising MeasureObjectIntensity
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity(null);
        measureObjectIntensity.initialiseParameters();
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,"Test_image");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,"Test_objects");
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
        // measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,false);

        // Running MeasureObjectIntensity
        measureObjectIntensity.execute(workspace);

        // Checking the workspace contains a single object set
        assertEquals(1,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjects(inputObjectsName));
        assertEquals(8,workspace.getObjects(inputObjectsName).size());

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MEAN_32BIT.name()).getValue();
            double actual = testObject.getMeasurement("INTENSITY // Test_image_MEAN").getValue();
            assertEquals(expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MIN_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MIN").getValue();
            assertEquals(expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_MAX_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_MAX").getValue();
            assertEquals(expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_STD_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_STDEV").getValue();
            assertEquals(expected, actual,1E-2);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_I_SUM_32BIT.name()).getValue();
            actual = testObject.getMeasurement("INTENSITY // Test_image_SUM").getValue();
            assertEquals(expected, actual,1E-2);

        }
    }

    // /**
    //  * This tests the mean and stdev intensity distance on a single object (approximately spherical) against a shell
    //  * of intensity 2px inside the object.  This test doesn't take differences in XY and Z scaling into account.
    //  * @throws Exception
    //  */
    // @ParameterizedTest
    // @EnumSource(VolumeType.class)
    // public void testMeasureWeightedEdgeDistance2pxShellInside(VolumeType volumeType) throws Exception {
    //     // Creating a new workspace
    //     Workspaces workspaces = new Workspaces();
    //     WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

    //     // Setting object parameters
    //     String inputObjectsName = "Test_objects";
    //     double dppXY = 0.02;
    //     double dppZ = 0.1;
    //     String calibratedUnits = "um";

    //     // Creating objects and adding to workspace
    //     Objs testObjects = new Sphere3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
    //     workspace.addObjects(testObjects);

    //     // Loading the test image and adding to workspace
    //     String imageName = "Test_image";
    //     String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureObjectIntensity/BinarySphere3D_1pxInside10pxOutsideShell_8bit.zip").getPath(),"UTF-8");
    //     ImagePlus ipl = IJ.openImage(pathToImage);
    //     Image intensityImage = ImageFactory.createImage(imageName,ipl);
    //     workspace.addImage(intensityImage);

    //     MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity(null);
    //     measureObjectIntensity.initialiseParameters();
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,imageName);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,inputObjectsName);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,true);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.EDGE_DISTANCE_MODE,MeasureObjectIntensity.EdgeDistanceModes.INSIDE_ONLY);

    //     // Running MeasureObjectIntensity
    //     measureObjectIntensity.execute(workspace);

    //     // Getting object (there is only one)
    //     Obj object = testObjects.values().iterator().next();
    //     String meanMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.MEAN_EDGE_DISTANCE_PX);
    //     String stdevMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.STD_EDGE_DISTANCE_PX);

    //     assertEquals(1,object.getMeasurement(meanMeasName).getValue(),0.5);
    //     assertEquals(0,object.getMeasurement(stdevMeasName).getValue(),0.5);

    // }

    // @ParameterizedTest
    // @EnumSource(VolumeType.class)
    // public void testMeasureWeightedEdgeDistance10pxShellOutside(VolumeType volumeType) throws Exception {
    //     // Creating a new workspace
    //     Workspaces workspaces = new Workspaces();
    //     WorkspaceI workspace = workspaces.getNewWorkspace(null,1);

    //     // Setting object parameters
    //     String inputObjectsName = "Test_objects";
    //     double dppXY = 0.02;
    //     double dppZ = 0.1;
    //     String calibratedUnits = "um";

    //     // Creating objects and adding to workspace
    //     Objs testObjects = new Sphere3D(volumeType).getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
    //     workspace.addObjects(testObjects);

    //     // Loading the test image and adding to workspace
    //     String imageName = "Test_image";
    //     String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/MeasureObjectIntensity/BinarySphere3D_1pxInside10pxOutsideShell_8bit.zip").getPath(),"UTF-8");
    //     ImagePlus ipl = IJ.openImage(pathToImage);
    //     Image intensityImage = ImageFactory.createImage(imageName,ipl);
    //     workspace.addImage(intensityImage);

    //     MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity(null);
    //     measureObjectIntensity.initialiseParameters();
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,imageName);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,inputObjectsName);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,false);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_EDGE_DISTANCE,true);
    //     measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.EDGE_DISTANCE_MODE,MeasureObjectIntensity.EdgeDistanceModes.OUTSIDE_ONLY);

    //     // Running MeasureObjectIntensity
    //     measureObjectIntensity.execute(workspace);

    //     // Getting object (there is only one)
    //     Obj object = testObjects.values().iterator().next();
    //     String meanMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.MEAN_EDGE_DISTANCE_PX);
    //     String stdevMeasName = MeasureObjectIntensity.getFullName(imageName,MeasureObjectIntensity.Measurements.STD_EDGE_DISTANCE_PX);

    //     assertEquals(10,object.getMeasurement(meanMeasName).getValue(),0.5);
    //     assertEquals(0,object.getMeasurement(stdevMeasName).getValue(),0.5);

    // }
}