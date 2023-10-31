package io.github.mianalysis.mia.module.objects.detect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URLDecoder;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Objects2D;
import io.github.mianalysis.mia.expectedobjects.Objects3D;
import io.github.mianalysis.mia.expectedobjects.Objects4D;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;

/**
 * Created by Stephen Cross on 29/08/2017.
 */

public class IdentifyObjectsTest extends ModuleTest {
    private static int nThreads = Prefs.getThreads();

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @BeforeAll
    public static void setThreadsBefore() {
        Prefs.setThreads(Math.min(3, nThreads));
    }

    @AfterAll
    public static void setThreadsAfter() {
        Prefs.setThreads(nThreads);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new IdentifyObjects(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC,
                IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects4D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects4D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects4D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    @Disabled
    public void testRunBlackBackground8bit5D() throws Exception {

    }

    /**
     * This tests that the system still works when presented with a labelled (rather
     * than binary) image.
     * 
     * @throws Exception
     */
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectPointListWithoutMT(VolumeType volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectQuadTreeWithoutMT(VolumeType volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectOcTreeWithoutMT(VolumeType volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DPointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DQuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DOcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255PointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255QuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255OcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535PointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535QuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535OcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1PointListWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1QuadTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1OcTreeWithoutMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects4D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects4D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects4D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    /**
     * This tests that the system still works when presented with a labelled (rather
     * than binary) image.
     * 
     * @throws Exception
     */
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectPointListWithMT(VolumeType volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DPointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DQuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DOcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255PointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255QuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255OcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535PointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535QuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535OcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1PointListWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1QuadTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1OcTreeWithMT(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = ImageFactory.createImage("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getObjects().size());

        // Getting the object set
        Objs actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        Objs expectedObjects = new Objects3D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (Obj object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

}