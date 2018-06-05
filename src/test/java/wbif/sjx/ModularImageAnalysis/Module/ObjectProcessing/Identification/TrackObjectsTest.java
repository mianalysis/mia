package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TrackObjectsTest {
    private double tolerance = 1E-10;

    @Test
    public void testGetCandidateObjectsBothPresent() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a collection of objects at different timepoints
        ObjCollection objects = new ObjCollection(inputObjectsName);
        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
        objects.add(obj1);
        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj2);
        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj3);
        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj4);
        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj5);
        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
        objects.add(obj6);
        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj7);
        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj8);
        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj9);
        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj10);
        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj11);
        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj12);

        // Getting candidate objects for timepoints 20 and 21
        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,20,21);

        // Checking the expected objects are there
        ArrayList<Obj> previous = candidates[0];
        assertEquals(2,previous.size());
        assertTrue(previous.contains(obj3));
        assertTrue(previous.contains(obj9));

        ArrayList<Obj> current = candidates[1];
        assertEquals(4,current.size());
        assertTrue(current.contains(obj2));
        assertTrue(current.contains(obj4));
        assertTrue(current.contains(obj7));
        assertTrue(current.contains(obj12));

    }

    @Test
    public void testGetCandidateObjectsBothPresentLargeGap() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a collection of objects at different timepoints
        ObjCollection objects = new ObjCollection(inputObjectsName);
        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
        objects.add(obj1);
        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj2);
        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj3);
        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj4);
        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj5);
        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
        objects.add(obj6);
        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj7);
        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj8);
        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj9);
        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj10);
        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj11);
        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj12);

        // Getting candidate objects for timepoints 20 and 21
        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,7,21);

        // Checking the expected objects are there
        ArrayList<Obj> previous = candidates[0];
        assertEquals(1,previous.size());
        assertTrue(previous.contains(obj1));

        ArrayList<Obj> current = candidates[1];
        assertEquals(4,current.size());
        assertTrue(current.contains(obj2));
        assertTrue(current.contains(obj4));
        assertTrue(current.contains(obj7));
        assertTrue(current.contains(obj12));

    }

    @Test
    public void testGetCandidateObjectsMissingT1() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a collection of objects at different timepoints
        ObjCollection objects = new ObjCollection(inputObjectsName);
        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
        objects.add(obj1);
        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj2);
        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(19);
        objects.add(obj3);
        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj4);
        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj5);
        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
        objects.add(obj6);
        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj7);
        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj8);
        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(19);
        objects.add(obj9);
        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj10);
        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj11);
        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj12);

        // Getting candidate objects for timepoints 20 and 21
        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,20,21);

        // Checking the expected objects are there
        ArrayList<Obj> previous = candidates[0];
        assertEquals(0,previous.size());

        ArrayList<Obj> current = candidates[1];
        assertEquals(4,current.size());
        assertTrue(current.contains(obj2));
        assertTrue(current.contains(obj4));
        assertTrue(current.contains(obj7));
        assertTrue(current.contains(obj12));
    }

    @Test
    public void testGetCandidateObjectsMissingT2() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a collection of objects at different timepoints
        ObjCollection objects = new ObjCollection(inputObjectsName);
        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
        objects.add(obj1);
        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(22);
        objects.add(obj2);
        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj3);
        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(22);
        objects.add(obj4);
        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj5);
        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
        objects.add(obj6);
        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(22);
        objects.add(obj7);
        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj8);
        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj9);
        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj10);
        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj11);
        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(22);
        objects.add(obj12);

        // Getting candidate objects for timepoints 20 and 21
        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,20,21);

        // Checking the expected objects are there
        ArrayList<Obj> previous = candidates[0];
        assertEquals(2,previous.size());
        assertTrue(previous.contains(obj3));
        assertTrue(previous.contains(obj9));

        ArrayList<Obj> current = candidates[1];
        assertEquals(0,current.size());

    }

    @Test
    public void testGetCandidateObjectsMissingBoth() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a collection of objects at different timepoints
        ObjCollection objects = new ObjCollection(inputObjectsName);
        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
        objects.add(obj1);
        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj2);
        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj3);
        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj4);
        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj5);
        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
        objects.add(obj6);
        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj7);
        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj8);
        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
        objects.add(obj9);
        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
        objects.add(obj10);
        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
        objects.add(obj11);
        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
        objects.add(obj12);

        // Getting candidate objects for timepoints 20 and 21
        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,40,41);

        // Checking the expected objects are there
        ArrayList<Obj> previous = candidates[0];
        assertEquals(0,previous.size());

        ArrayList<Obj> current = candidates[1];
        assertEquals(0,current.size());

    }

    @Test @Ignore
    public void testCalculateCostMatrixCentroid() {
    }

    @Test @Ignore
    public void testCalculateCostMatrixOverlap() {
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