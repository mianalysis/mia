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
import io.github.mianalysis.mia.expectedobjects.VolumeTypes;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.volume.CoordinateSetFactoryI;
import io.github.mianalysis.mia.object.coordinates.volume.OctreeFactory;
import io.github.mianalysis.mia.object.coordinates.volume.PointListFactory;
import io.github.mianalysis.mia.object.coordinates.volume.QuadtreeFactory;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.image.ImageI;

/**
 * Created by Stephen Cross on 29/08/2017.
 */

public class IdentifyObjectsTest extends ModuleTest {
    private static int nThreads = Prefs.getThreads();


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
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit2DPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects2D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit2DQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC,
                IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects2D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit2DOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects2D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit4DPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects4D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit4DQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects4D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit4DOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects4D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
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
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DLabelledPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DLabelledQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DLabelledOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWhiteBackground8Bit3DPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWhiteBackground8Bit3DQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWhiteBackground8Bit3DOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DSingleObjectPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DSingleObjectQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DSingleObjectOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectPointListWithoutMT(VolumeTypes volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectQuadtreeWithoutMT(VolumeTypes volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectOctreeWithoutMT(VolumeTypes volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DPointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DQuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DOctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DNot255PointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DNot255QuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DNot255OctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DNot65535PointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DNot65535QuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DNot65535OctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DNot1PointListWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DNot1QuadtreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DNot1OctreeWithoutMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit2DPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects2D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit2DQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects2D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit2DOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects2D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects2D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit4DPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects4D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit4DQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects4D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit4DOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects4D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects4D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(33, actualObjects.size());

        int count = 0;
        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
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
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DLabelledPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DLabelledQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DLabelledOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWhiteBackground8Bit3DPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWhiteBackground8Bit3DQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunWhiteBackground8Bit3DOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_whiteBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.WHITE_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DSingleObjectPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DSingleObjectQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DSingleObjectOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectPointListWithMT(VolumeTypes volumeType)
            throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/labelledobjects/LabelledObjects3D_8bit.zip").getPath(), "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.BINARY,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(1, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected",
                ExpectedObjects.Mode.SIXTEEN_BIT, dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DPointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DQuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DOctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DNot255PointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DNot255QuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground8Bit3DNot255OctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_8bit_blackBG_204.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DNot65535PointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DNot65535QuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground16Bit3DNot65535OctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_16bit_blackBG_15073.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DNot1PointListWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new PointListFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DNot1QuadtreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new QuadtreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeTypes.class)
    public void testRunBlackBackground32Bit3DNot1OctreeWithMT(VolumeTypes volumeType) throws Exception {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        WorkspaceI workspace = workspaces.getNewWorkspace(null, 1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(
                this.getClass().getResource("/images/binaryobjects/BinaryObjects3D_32bit_blackBG_-0p54.zip").getPath(),
                "UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        ImageI image = ImageFactories.getDefaultFactory().create("Test_image", ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE, "Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS, "Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.BINARY_LOGIC, IdentifyObjects.BinaryLogic.BLACK_BACKGROUND);
        identifyObjects.updateParameterValue(IdentifyObjects.ENABLE_MULTITHREADING, true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE, new OctreeFactory().getName());

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1, workspace.getAllObjects().size());

        // Getting the object set
        ObjsI actualObjects = workspace.getObjects("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects", actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can
        // interpret 32-bit images
        CoordinateSetFactoryI factory = VolumeTypes.getFactory(volumeType);
        ObjsI expectedObjects = new Objects3D(factory).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,
                dppXY, dppZ, calibratedUnits, true);

        // Checking the number of detected objects
        assertEquals(8, actualObjects.size());

        for (ObjI object : actualObjects.values()) {
            // Identifying the matching object. If this is null, one isn't found
            ObjI expectedObject = expectedObjects.getByEqualsIgnoreNameAndID(object);
            assertNotNull(expectedObject);

        }
    }

}