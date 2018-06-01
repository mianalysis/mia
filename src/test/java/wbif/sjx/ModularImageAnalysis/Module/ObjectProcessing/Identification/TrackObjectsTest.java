package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import static org.junit.Assert.*;

public class TrackObjectsTest {
    private double tolerance = 1E-10;

    @Test @Ignore
    public void testGetCandidateObjects() {
    }

    @Test @Ignore
    public void testCalculateCostMatrix() {
    }

    @Test @Ignore
    public void testTestLinkValidity() {
    }

    @Test @Ignore
    public void testGetCentroidSeparation() {
    }

    @Test @Ignore
    public void testGetAbsoluteOverlap() {
    }

    @Test
    public void testLinkObjects() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating two objects and a track associated with the first object
        Obj obj1 = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false);
        Obj obj2 = new Obj(inputObjectsName,21,dppXY,dppZ,calibratedUnits,false);
        Obj track = new Obj(trackObjectsName,4,dppXY,dppZ,calibratedUnits,false);
        obj1.addParent(track);

        // Creating the TrackObjects object
        TrackObjects trackObjects = new TrackObjects();
        trackObjects.initialiseParameters();
        trackObjects.updateParameterValue(TrackObjects.TRACK_OBJECTS,trackObjectsName);

        // Linking the objects
        trackObjects.linkObjects(obj1,obj2);

        // Checking the two objects are associated with each other
        assertEquals(21,obj1.getMeasurement(TrackObjects.Measurements.TRACK_NEXT_ID).getValue(),tolerance);
        assertNull(obj1.getMeasurement(TrackObjects.Measurements.TRACK_PREV_ID));
        assertEquals(7,obj2.getMeasurement(TrackObjects.Measurements.TRACK_PREV_ID).getValue(),tolerance);
        assertNull(obj2.getMeasurement(TrackObjects.Measurements.TRACK_NEXT_ID));

        // Checking the second object inherited the track from the first
        assertEquals(4,obj2.getParent(trackObjectsName).getID());

    }

    @Test
    public void testCreateNewTrack() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating an object and the track objects collection for it to be added to
        Obj obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false);
        ObjCollection tracks = new ObjCollection(trackObjectsName);

        // Creating the TrackObjects object
        TrackObjects trackObjects = new TrackObjects();
        trackObjects.initialiseParameters();

        // Checking the track collection is empty
        assertEquals(0,tracks.size());

        // Creating the new track
        trackObjects.createNewTrack(obj, tracks);

        // Checking the track collection has increased by 1
        assertEquals(1,tracks.size());

        // Checking the input object is associated with the new track
        assertEquals(5,tracks.getFirst().getChildren(inputObjectsName).getFirst().getID());
        assertNotNull(obj.getParent(trackObjectsName));
        assertNull(obj.getParent("fake name"));

    }

    @Test @Ignore
    public void testIdentifyLeading() {
    }

    @Test
    public void testGetTitle() {
        assertNotNull(new TrackObjects().getTitle());
    }

    @Test @Ignore
    public void testRun() {
        // We're going to need loads of these
    }
}