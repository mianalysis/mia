package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import ij.IJ;
import ij.ImagePlus;
import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 07/12/2017.
 */
public class FilterObjectsTest extends ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new FilterObjects().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FilterObjects().getHelp());
    }

    @Test
    public void testRunMeasurementsLargerThan() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.MEASUREMENTS_LARGER_THAN);
        filterObjects.updateParameterValue(FilterObjects.MEASUREMENT, Objects3D.Measures.EXP_N_VOXELS.name());
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_VALUE,200d);

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(5,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunMeasurementsSmallerThan() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.MEASUREMENTS_SMALLER_THAN);
        filterObjects.updateParameterValue(FilterObjects.MEASUREMENT, Objects3D.Measures.EXP_N_VOXELS.name());
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_VALUE,200d);

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunMissingMeasurement() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.MISSING_MEASUREMENTS);
        filterObjects.updateParameterValue(FilterObjects.MEASUREMENT, Objects3D.Measures.EXP_I_STD_8BIT.name());
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_VALUE,200d);

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(7,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunObjectsOnImageEdge() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.REMOVE_ON_IMAGE_EDGE_2D);
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_IMAGE,"Test_image");

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(7,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunObjectsOnImageEdgeIgnoreZ() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.REMOVE_ON_IMAGE_EDGE_2D);
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_IMAGE,"Test_image");
        filterObjects.updateParameterValue(FilterObjects.INCLUDE_Z_POSITION,false);

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(7,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunObjectsOnImageEdgeIncludeZ() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.REMOVE_ON_IMAGE_EDGE_2D);
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_IMAGE,"Test_image");
        filterObjects.updateParameterValue(FilterObjects.INCLUDE_Z_POSITION,true);

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(4,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunMinimumNumberOfChildren() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.  The number of children will be
        // created according to the "kids" table - it doesn't matter which test objects these are assigned to, as we
        // only count the number of remaining objects post-filter.
        int[] kids = new int[]{3,1,4,2,0,6,5,2};
        ObjCollection childObjects = new ObjCollection("Children");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            for (int i=0;i<kids[counter];i++) {
                Obj childObject = new Obj("Children", childObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                childObjects.add(childObject);

                testObject.addChild(childObject);
                childObject.addParent(testObject);
            }

            counter++;
        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.MIN_NUMBER_OF_CHILDREN);
        filterObjects.updateParameterValue(FilterObjects.CHILD_OBJECTS,"Children");
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_VALUE,2d);

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(6,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunMaximumNumberOfChildren() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.  The number of children will be
        // created according to the "kids" table - it doesn't matter which test objects these are assigned to, as we
        // only count the number of remaining objects post-filter.
        int[] kids = new int[]{3,1,4,2,0,6,5,2};
        ObjCollection childObjects = new ObjCollection("Children");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            for (int i=0;i<kids[counter];i++) {
                Obj childObject = new Obj("Children", childObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                childObjects.add(childObject);

                testObject.addChild(childObject);
                childObject.addParent(testObject);
            }

            counter++;

        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.MAX_NUMBER_OF_CHILDREN);
        filterObjects.updateParameterValue(FilterObjects.CHILD_OBJECTS,"Children");
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_VALUE,2d);

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(4,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunMissingParent() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,true};
        ObjCollection parentObjects = new ObjCollection("Parents");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.NO_PARENT);
        filterObjects.updateParameterValue(FilterObjects.PARENT_OBJECT,"Parents");

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(4,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunWithParent() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,true,true};
        ObjCollection parentObjects = new ObjCollection("Parents");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.WITH_PARENT);
        filterObjects.updateParameterValue(FilterObjects.PARENT_OBJECT,"Parents");

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());

    }
}