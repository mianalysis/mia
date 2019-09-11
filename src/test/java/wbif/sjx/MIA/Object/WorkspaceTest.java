package wbif.sjx.MIA.Object;

import ij.ImagePlus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.VolumeType;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class WorkspaceTest {
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testAddObject(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a new workspace and checking it is empty
        Workspace workspace = new Workspace(0,null,0);
        assertEquals(0,workspace.getObjects().size());

        // Creating and adding the new object
        Obj obj = new Obj(volumeType,"New obj",0,20,10,5,dppXY,dppZ,calibratedUnits);
        workspace.addObject(obj);

        // Checking the workspace behaved as expected
        assertEquals(1,workspace.getObjects().size());
        assertNotNull(workspace.getObjectSet("New obj"));
        assertNull(workspace.getObjectSet("Neww obj"));
        assertEquals(1,workspace.getObjectSet("New obj").size());

    }

    @Test
    public void testClearAllImagesDoRetainMeasurements() {
        Workspace workspace = new Workspace(0,null,0);

        // Adding images
        ImagePlus imagePlus = new ImagePlus();
        Image image = new Image("Test im",imagePlus);
        image.addMeasurement(new Measurement("Test meas",4.6));
        workspace.addImage(image);

        imagePlus = new ImagePlus();
        image = new Image("Test im2",imagePlus);
        image.addMeasurement(new Measurement("Test meas",4.6));
        workspace.addImage(image);

        // Checking workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test im").getImagePlus());
        assertEquals(1,workspace.getImage("Test im").getMeasurements().size());
        assertNull(workspace.getImage("Testt im"));
        assertNotNull(workspace.getImage("Test im2").getImagePlus());
        assertEquals(1,workspace.getImage("Test im2").getMeasurements().size());

        // Clearing images
        workspace.clearAllImages(true);

        // Checking workspace after clear
        assertEquals(2,workspace.getImages().size());
        assertNull(workspace.getImage("Test im").getImagePlus());
        assertEquals(1,workspace.getImage("Test im").getMeasurements().size());
        assertNull(workspace.getImage("Test im2").getImagePlus());
        assertEquals(1,workspace.getImage("Test im2").getMeasurements().size());

    }

    @Test
    public void testClearAllImagesDontRetainMeasurements() {
        Workspace workspace = new Workspace(0,null,0);

        // Adding images
        ImagePlus imagePlus = new ImagePlus();
        Image image = new Image("Test im",imagePlus);
        image.addMeasurement(new Measurement("Test meas",4.6));
        workspace.addImage(image);

        imagePlus = new ImagePlus();
        image = new Image("Test im2",imagePlus);
        image.addMeasurement(new Measurement("Test meas",4.6));
        workspace.addImage(image);

        // Checking workspace
        assertEquals(2,workspace.getImages().size());
        assertNotNull(workspace.getImage("Test im").getImagePlus());
        assertEquals(1,workspace.getImage("Test im").getMeasurements().size());
        assertNull(workspace.getImage("Testt im"));
        assertNotNull(workspace.getImage("Test im2").getImagePlus());
        assertEquals(1,workspace.getImage("Test im2").getMeasurements().size());

        // Clearing images
        workspace.clearAllImages(false);

        // Checking workspace after clear
        assertEquals(0,workspace.getImages().size());
        assertNull(workspace.getImage("Test im"));
        assertNull(workspace.getImage("Test im2"));
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testClearAllObjectsDoRetainMeasurements(VolumeType volumeType) throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a new workspace and populating it with a set of objects
        Workspace workspace = new Workspace(0,null,0);

        Obj obj = new Obj(volumeType,"New obj",0,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        obj = new Obj(volumeType,"New obj",1,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        obj = new Obj(volumeType,"Other obj",0,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        obj = new Obj(volumeType,"Other obj",1,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        // Checking current state of the workspace
        assertEquals(2,workspace.getObjects().size());
        assertEquals(1,workspace.getObjectSet("New obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("New obj").getFirst().getPoints().size());
        assertTrue(workspace.getObjectSet("New obj").getFirst().getSurfaceXCoords().size() != 0);
        assertEquals(1,workspace.getObjectSet("Other obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("Other obj").getFirst().getPoints().size());
        assertTrue(workspace.getObjectSet("Other obj").getFirst().getSurfaceXCoords().size() != 0);

        // Clearing objects
        workspace.clearAllObjects(true);

        // Checking post-clear state of the workspace
        assertEquals(2,workspace.getObjects().size());
        assertEquals(1,workspace.getObjectSet("New obj").getFirst().getMeasurements().size());
        assertEquals(0,workspace.getObjectSet("New obj").getFirst().getPoints().size());
        assertTrue(workspace.getObjectSet("New obj").getFirst().getSurfaceXCoords().size() == 0);
        assertEquals(1,workspace.getObjectSet("Other obj").getFirst().getMeasurements().size());
        assertEquals(0,workspace.getObjectSet("Other obj").getFirst().getPoints().size());
        assertTrue(workspace.getObjectSet("Other obj").getFirst().getSurfaceXCoords().size() == 0);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testClearAllObjectsDontRetainMeasurements(VolumeType volumeType) throws IntegerOverflowException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a new workspace and populating it with a set of objects
        Workspace workspace = new Workspace(0,null,0);

        Obj obj = new Obj(volumeType,"New obj",0,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        obj = new Obj(volumeType,"New obj",1,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        obj = new Obj(volumeType,"Other obj",0,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        obj = new Obj(volumeType,"Other obj",1,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObject(obj);

        // Checking current state of the workspace
        assertEquals(2,workspace.getObjects().size());
        assertEquals(1,workspace.getObjectSet("New obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("New obj").getFirst().getPoints().size());
        assertTrue(workspace.getObjectSet("New obj").getFirst().getSurfaceXCoords().size() != 0);
        assertEquals(1,workspace.getObjectSet("Other obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("Other obj").getFirst().getPoints().size());
        assertTrue(workspace.getObjectSet("Other obj").getFirst().getSurfaceXCoords().size() != 0);

        // Clearing objects
        workspace.clearAllObjects(false);

        // Checking post-clear state of the workspace
        assertEquals(0,workspace.getObjects().size());
        assertNull(workspace.getObjectSet("New obj"));
        assertNull(workspace.getObjectSet("Other obj"));

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testGetSingleTimepointWorkspaces(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a new workspace and populating it with a set of objects
        Workspace workspace = new Workspace(0,null,0);

        Obj obj = new Obj(volumeType,"New obj",0,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.setT(0);
        workspace.addObject(obj);

        obj = new Obj(volumeType,"New obj",1,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.setT(1);
        workspace.addObject(obj);

        obj = new Obj(volumeType,"Other obj",0,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.setT(0);
        workspace.addObject(obj);

        obj = new Obj(volumeType,"Other obj",1,20,10,5,dppXY,dppZ,calibratedUnits);
        obj.setT(2);
        workspace.addObject(obj);

        // Getting single timepoint workspaces
        HashMap<Integer, Workspace> singleTimepoints = workspace.getSingleTimepointWorkspaces();

        assertEquals(3,singleTimepoints.size());
        assertEquals(2,singleTimepoints.get(0).getObjects().size());
        assertEquals(1,singleTimepoints.get(0).getObjectSet("New obj").size());
        assertEquals(1,singleTimepoints.get(0).getObjectSet("Other obj").size());
        assertEquals(1,singleTimepoints.get(1).getObjects().size());
        assertEquals(1,singleTimepoints.get(1).getObjectSet("New obj").size());
        assertNull(singleTimepoints.get(1).getObjectSet("Other obj"));
        assertEquals(1,singleTimepoints.get(2).getObjects().size());
        assertNull(singleTimepoints.get(2).getObjectSet("New obj"));
        assertEquals(1,singleTimepoints.get(2).getObjectSet("Other obj").size());

    }
}