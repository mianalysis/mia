package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImagePlus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects2D;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.ExpectedObjects.Objects4D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.net.URLDecoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Stephen Cross on 29/08/2017.
 */
public class IdentifyObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new IdentifyObjects(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DPointList(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DQuadTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit2DOcTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects2D(volumeType).getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DPointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DQuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DOcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DPointList(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects4D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(33,actualObjects.size());

        int count = 0;
        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DQuadTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects4D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(33,actualObjects.size());

        int count = 0;
        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit4DOcTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects4D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(33,actualObjects.size());

        int count = 0;
        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test @Disabled
    public void testRunBlackBackground8bit5D() throws Exception  {

    }

    /**
     * This tests that the system still works when presented with a labelled (rather than binary) image.
     * @throws Exception
     */
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledPointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledQuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DLabelledOcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DPointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DQuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWhiteBackground8Bit3DOcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectPointList(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectQuadTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DSingleObjectOcTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectPointList(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectQuadTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8bit3DLabelledSingleObjectOcTree(VolumeType volumeType) throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DPointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.SIXTEEN_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DQuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.SIXTEEN_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DOcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.SIXTEEN_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DPointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can interpret 32-bit images
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DQuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can interpret 32-bit images
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DOcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can interpret 32-bit images
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255PointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG_204.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255QuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG_204.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground8Bit3DNot255OcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG_204.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535PointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG_15073.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535QuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG_15073.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground16Bit3DNot65535OcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG_15073.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1PointList(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG_-0p54.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.POINTLIST);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can interpret 32-bit images
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1QuadTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG_-0p54.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.QUADTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can interpret 32-bit images
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunBlackBackground32Bit3DNot1OcTree(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG_-0p54.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects(null);
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.VOLUME_TYPE,IdentifyObjects.VolumeTypes.OCTREE);

        // Running IdentifyObjects
        identifyObjects.execute(workspace);

        // Checking there is only one set of objects in the workspace
        assertEquals(1,workspace.getObjects().size());

        // Getting the object set
        ObjCollection actualObjects = workspace.getObjectSet("Test_output_objects");

        // Checking the expected object set is present
        assertEquals("Test_output_objects",actualObjects.getName());

        // Getting the expected objects
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Bit depth here doesn't matter - we only want to check the module can interpret 32-bit images
        ObjCollection expectedObjects = new Objects3D(volumeType).getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }
}