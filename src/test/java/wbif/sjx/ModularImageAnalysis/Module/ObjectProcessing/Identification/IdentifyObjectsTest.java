package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects2D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Objects4D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Process.ColourFactory;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 29/08/2017.
 */
public class IdentifyObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new IdentifyObjects().getTitle());
    }

    @Test
    public void testRunBlackBackground8bit2D() throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects2D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects2D().getObjects("Expected", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground8Bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground8bit4D() throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects4D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects4D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(33,actualObjects.size());

        int count = 0;
        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test @Ignore
    public void testRunBlackBackground8bit5D() throws Exception  {

    }

    /**
     * This tests that the system still works when presented with a labelled (rather than binary) image.
     * @throws Exception
     */
    @Test
    public void testRunBlackBackground8Bit3DLabelled() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunWhiteBackground8Bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_whiteBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,true);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground8bit3DSingleObject() throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground8bit3DLabelledSingleObject() throws Exception  {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/LabelledObjects/LabelledObjects3D_8bit.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);
        identifyObjects.updateParameterValue(IdentifyObjects.SINGLE_OBJECT,true);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.BINARY,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(1,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground16Bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.SIXTEEN_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground32Bit3D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground8Bit3DNot255() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_8bit_blackBG_204.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground16Bit3DNot65535() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_16bit_blackBG_15073.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }

    @Test
    public void testRunBlackBackground32Bit3DNot1() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects3D_32bit_blackBG_-0p54.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising IdentifyObjects
        IdentifyObjects identifyObjects = new IdentifyObjects();
        identifyObjects.initialiseParameters();
        identifyObjects.updateParameterValue(IdentifyObjects.INPUT_IMAGE,"Test_image");
        identifyObjects.updateParameterValue(IdentifyObjects.OUTPUT_OBJECTS,"Test_output_objects");
        identifyObjects.updateParameterValue(IdentifyObjects.WHITE_BACKGROUND,false);

        // Running IdentifyObjects
        identifyObjects.run(workspace);

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
        ObjCollection expectedObjects = new Objects3D().getObjects("Expected",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);

        // Checking the number of detected objects
        assertEquals(8,actualObjects.size());

        for (Obj object:actualObjects.values()) {
            // Identifying the matching object.  If this is null, one isn't found
            Obj expectedObject = expectedObjects.getByEquals(object);
            assertNotNull(expectedObject);

        }
    }
}