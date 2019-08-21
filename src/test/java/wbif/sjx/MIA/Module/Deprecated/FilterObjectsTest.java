package wbif.sjx.MIA.Module.Deprecated;

import ij.IJ;
import ij.ImagePlus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.net.URLDecoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by sc13967 on 07/12/2017.
 */
public class FilterObjectsTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FilterObjects(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMeasurementsLargerThan(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMeasurementsSmallerThan(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingMeasurement(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunObjectsOnImageEdge(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.REMOVE_ON_IMAGE_EDGE_2D);
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_IMAGE,"Test_image");

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(7,workspace.getObjectSet("TestObj").values().size());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunObjectsOnImageEdgeIgnoreZ(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunObjectsOnImageEdgeIncludeZ(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Loading the test image and adding to workspace
        String pathToImage = URLDecoder.decode(this.getClass().getResource("/images/BinaryObjects/BinaryObjects5D_8bit_blackBG.tif").getPath(),"UTF-8");
        ImagePlus ipl = IJ.openImage(pathToImage);
        Image image = new Image("Test_image",ipl);
        workspace.addImage(image);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMinimumNumberOfChildren(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.  The number of children will be
        // created according to the "kids" table - it doesn't matter which test objects these are assigned to, as we
        // only count the number of remaining objects post-filter.
        int[] kids = new int[]{3,1,4,2,0,6,5,2};
        ObjCollection childObjects = new ObjCollection("Children");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            for (int i=0;i<kids[counter];i++) {
                Obj childObject = new Obj(volumeType,"Children", childObjects.getAndIncrementID(),1,1,1,dppXY, dppZ, calibratedUnits);
                childObjects.add(childObject);

                testObject.addChild(childObject);
                childObject.addParent(testObject);
            }

            counter++;
        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMaximumNumberOfChildren(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.  The number of children will be
        // created according to the "kids" table - it doesn't matter which test objects these are assigned to, as we
        // only count the number of remaining objects post-filter.
        int[] kids = new int[]{3,1,4,2,0,6,5,2};
        ObjCollection childObjects = new ObjCollection("Children");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            for (int i=0;i<kids[counter];i++) {
                Obj childObject = new Obj(volumeType,"Children", childObjects.getAndIncrementID(),1,1,1,dppXY, dppZ, calibratedUnits);
                childObjects.add(childObject);

                testObject.addChild(childObject);
                childObject.addParent(testObject);
            }

            counter++;

        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunMissingParent(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,true};
        ObjCollection parentObjects = new ObjCollection("Parents");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents", parentObjects.getAndIncrementID(),1,1,1, dppXY, dppZ, calibratedUnits);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.NO_PARENT);
        filterObjects.updateParameterValue(FilterObjects.PARENT_OBJECT,"Parents");

        // Running the module
        filterObjects.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(4,workspace.getObjectSet("TestObj").values().size());

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testRunWithParent(VolumeType volumeType) throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D(volumeType).getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,true,true};
        ObjCollection parentObjects = new ObjCollection("Parents");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj(volumeType,"Parents", parentObjects.getAndIncrementID(),1,1,1, dppXY, dppZ, calibratedUnits);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects(new ModuleCollection());
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