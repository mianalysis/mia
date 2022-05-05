package io.github.mianalysis.mia.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import ij.ImagePlus;
import ome.units.UNITS;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.sjcross.sjcommon.exceptions.IntegerOverflowException;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

public class WorkspaceTest {
    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testAddObject(VolumeType volumeType) {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a new workspace and checking it is empty
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);
        assertEquals(0, workspace.getObjects().size());
        
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,20,10,5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);

        // Creating and adding the new object
        objects.createAndAddNewObject(volumeType,0);
        workspace.addObjects(objects);

        // Checking the workspace behaved as expected
        assertEquals(1,workspace.getObjects().size());
        assertNotNull(workspace.getObjectSet("Obj"));
        assertNull(workspace.getObjectSet("Neww obj"));
        assertEquals(1,workspace.getObjectSet("Obj").size());

    }

    @Test
    public void testClearAllImagesDoRetainMeasurements() {
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Adding images
        ImagePlus imagePlus = new ImagePlus();
        Image image = ImageFactory.createImage("Test im",imagePlus);
        image.addMeasurement(new Measurement("Test meas",4.6));
        workspace.addImage(image);

        imagePlus = new ImagePlus();
        image = ImageFactory.createImage("Test im2",imagePlus);
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
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);
        
        // Adding images
        ImagePlus imagePlus = new ImagePlus();
        Image image = ImageFactory.createImage("Test im",imagePlus);
        image.addMeasurement(new Measurement("Test meas",4.6));
        workspace.addImage(image);

        imagePlus = new ImagePlus();
        image = ImageFactory.createImage("Test im2",imagePlus);
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
    public void testClearAllObjectsDoRetainMeasurements(VolumeType volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a new workspace and populating it with a set of objects
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);
        
        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 20, 10, 5);
        
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj = objects.createAndAddNewObject(volumeType,0);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        
        obj = objects.createAndAddNewObject(volumeType,1);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObjects(objects);

        Objs otherObjects = new Objs("Other obj", calibration, 1, 0.02, UNITS.SECOND);
        obj = otherObjects.createAndAddNewObject(volumeType,0);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        
        obj = otherObjects.createAndAddNewObject(volumeType,1);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObjects(otherObjects);

        // Checking current state of the workspace
        assertEquals(2,workspace.getObjects().size());
        assertEquals(1,workspace.getObjectSet("Obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("Obj").getFirst().size());
        assertTrue(workspace.getObjectSet("Obj").getFirst().getSurfaceXCoords().size() != 0);
        assertEquals(1,workspace.getObjectSet("Other obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("Other obj").getFirst().size());
        assertTrue(workspace.getObjectSet("Other obj").getFirst().getSurfaceXCoords().size() != 0);

        // Clearing objects
        workspace.clearAllObjects(true);

        // Checking post-clear state of the workspace
        assertEquals(2,workspace.getObjects().size());
        assertEquals(1,workspace.getObjectSet("Obj").getFirst().getMeasurements().size());
        assertEquals(0,workspace.getObjectSet("Obj").getFirst().size());
        assertTrue(workspace.getObjectSet("Obj").getFirst().getSurfaceXCoords().size() == 0);
        assertEquals(1,workspace.getObjectSet("Other obj").getFirst().getMeasurements().size());
        assertEquals(0,workspace.getObjectSet("Other obj").getFirst().size());
        assertTrue(workspace.getObjectSet("Other obj").getFirst().getSurfaceXCoords().size() == 0);

    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    public void testClearAllObjectsDontRetainMeasurements(VolumeType volumeType) throws IntegerOverflowException, PointOutOfRangeException {
        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a new workspace and populating it with a set of objects
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);

        SpatCal calibration = new SpatCal(dppXY, dppZ, calibratedUnits, 20, 10, 5);
        
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj = objects.createAndAddNewObject(volumeType,0);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));

        obj = objects.createAndAddNewObject(volumeType,1);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObjects(objects);

        Objs otherObjects = new Objs("Other obj", calibration, 1, 0.02, UNITS.SECOND);
        obj = otherObjects.createAndAddNewObject(volumeType,0);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));

        obj = otherObjects.createAndAddNewObject(volumeType,1);
        obj.add(12,5,2);
        obj.add(12,5,3);
        obj.add(12,6,2);
        obj.addMeasurement(new Measurement("Test meas",1.5));
        workspace.addObjects(otherObjects);

        // Checking current state of the workspace
        assertEquals(2,workspace.getObjects().size());
        assertEquals(1,workspace.getObjectSet("Obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("Obj").getFirst().size());
        assertTrue(workspace.getObjectSet("Obj").getFirst().getSurface().size() != 0);
        assertEquals(1,workspace.getObjectSet("Other obj").getFirst().getMeasurements().size());
        assertEquals(3,workspace.getObjectSet("Other obj").getFirst().size());
        assertTrue(workspace.getObjectSet("Other obj").getFirst().getSurface().size() != 0);

        // Clearing objects
        workspace.clearAllObjects(false);

        // Checking post-clear state of the workspace
        assertEquals(0,workspace.getObjects().size());
        assertNull(workspace.getObjectSet("Obj"));
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
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null, 1);
        
        SpatCal calibration = new SpatCal(dppXY,dppZ,calibratedUnits,20,10,5);
        Objs objects = new Objs("Obj", calibration, 1, 0.02, UNITS.SECOND);
        Obj obj = objects.createAndAddNewObject(volumeType,0);
        obj.setT(0);
        obj = objects.createAndAddNewObject(volumeType,1);
        obj.setT(1);
        workspace.addObjects(objects);

        Objs otherObjects = new Objs("Other obj", calibration, 1, 0.02, UNITS.SECOND);
        obj = otherObjects.createAndAddNewObject(volumeType,0);
        obj.setT(0);
        obj = otherObjects.createAndAddNewObject(volumeType,1);
        obj.setT(2);
        workspace.addObjects(otherObjects);

        // Getting single timepoint workspaces
        HashMap<Integer, Workspace> singleTimepoints = workspace.getSingleTimepointWorkspaces();

        assertEquals(3,singleTimepoints.size());
        assertEquals(2, singleTimepoints.get(0).getObjects().size());
        assertEquals(1,singleTimepoints.get(0).getObjectSet("Obj").size());
        assertEquals(1,singleTimepoints.get(0).getObjectSet("Other obj").size());
        assertEquals(1,singleTimepoints.get(1).getObjects().size());
        assertEquals(1,singleTimepoints.get(1).getObjectSet("Obj").size());
        assertNull(singleTimepoints.get(1).getObjectSet("Other obj"));
        assertEquals(1,singleTimepoints.get(2).getObjects().size());
        assertNull(singleTimepoints.get(2).getObjectSet("Obj"));
        assertEquals(1,singleTimepoints.get(2).getObjectSet("Other obj").size());

    }
}