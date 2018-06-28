// TODO: Tests for with/without extra measures for centroid linking (volume and measurement weighting)

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TrackObjectsTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }


    // TESTING getCandidateObjects

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


    // TESTING getCentroidSeparation

    @Test
    public void testGetCentroidSeparation() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj1.addCoord(10,20,30);
        obj1.addCoord(11,20,31);
        obj1.addCoord(10,21,31);
        // Centroid @ [10.3333,20.3333,30.6667]

        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj2.addCoord(43,30,25);

        Obj obj3 = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj3.addCoord(12,25,30);

        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj4.addCoord(32,2,20);

        Obj obj5 = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj5.addCoord(21,39,25);
        obj5.addCoord(21,40,26);
        obj5.addCoord(22,40,27);
        // Centroid @ [21.3333,39.6667,26]

        Obj obj6 = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj6.addCoord(22,3,10);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        float actual = trackObjects.getCentroidSeparation(obj1,obj4,true);
        float expected = 60.4154f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj1,obj5,true);
        expected = 32.2371f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj1,obj6,true);
        expected = 105.4247f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj2,obj4,true);
        expected = 39.1152f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj2,obj5,true);
        expected = 24.2465f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj2,obj6,true);
        expected = 82.4318f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj3,obj4,true);
        expected = 58.5577f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj3,obj5,true);
        expected = 26.4995f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj3,obj6,true);
        expected = 102.8786f;
        assertEquals(expected,actual, tolerance);

    }

    @Test
    public void testGetCentroidSeparationOverlappingPoints() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj1.addCoord(10,20,30);
        obj1.addCoord(11,20,31);
        obj1.addCoord(10,21,31);
        // Centroid @ [10.3333,20.3333,30.6667]

        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj2.addCoord(43,30,25);

        Obj obj3 = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj3.addCoord(12,25,30);

        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj4.addCoord(43,30,25);

        Obj obj5 = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj5.addCoord(21,39,25);
        obj5.addCoord(21,40,26);
        obj5.addCoord(22,40,27);
        // Centroid @ [21.3333,39.6667,26]

        Obj obj6 = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj6.addCoord(12,25,30);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        float actual = trackObjects.getCentroidSeparation(obj1,obj4,true);
        float expected = 44.3097f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj1,obj5,true);
        expected = 32.2371f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj1,obj6,true);
        expected = 5.9723f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj2,obj4,true);
        expected = 0f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj2,obj5,true);
        expected = 24.2465f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj2,obj6,true);
        expected = 40.1373f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj3,obj4,true);
        expected = 40.1373f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj3,obj5,true);
        expected = 26.4995f;
        assertEquals(expected,actual, tolerance);

        actual = trackObjects.getCentroidSeparation(obj3,obj6,true);
        expected = 0f;
        assertEquals(expected,actual, tolerance);
    }

    @Test @Ignore
    public void testGetCentroidSeparationUseVolumeWeighting1() {

    }

    @Test @Ignore
    public void testGetCentroidSeparationUseVolumeWeighting5() {

    }

    @Test @Ignore
    public void testGetCentroidSeparationUseMeasurementWeighting1() {

    }

    @Test @Ignore
    public void testGetCentroidSeparationUseMeasurementWeighting5() {

    }


    // TESTING getAbsoluteOverlap

    @Test @Ignore
    public void testGetAbsoluteOverlap() {
    }


    // TESTING calculateCostMatrix FOR CENTROID MODE

    @Test
    public void testCalculateCostMatrixCentroid() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        ArrayList<Obj> previous = new ArrayList<>();
        Obj obj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(10,20,30);
        obj.addCoord(11,20,31);
        obj.addCoord(10,21,31);
        previous.add(obj);

        obj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(43,30,25);
        previous.add(obj);

        obj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(12,25,30);
        previous.add(obj);

        ArrayList<Obj> current = new ArrayList<>();
        obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(32,2,20);
        current.add(obj);

        obj = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(21,39,25);
        obj.addCoord(21,40,26);
        obj.addCoord(22,40,27);
        current.add(obj);

        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(22,3,10);
        current.add(obj);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        float[][] actual = trackObjects.calculateCostMatrix(previous,current,null);
        float[][] expected = new float[][]{
                {60.4154f,39.1152f,58.5577f},
                {32.2371f,24.2465f,26.4995f},
                {105.4247f,82.4318f,102.8786f}};

        assertEquals(3,actual.length);
        assertEquals(3,actual[1].length);

        for (int i=0;i<expected.length;i++) {
            for (int j=0;j<expected[i].length;j++) {
                assertEquals(expected[i][j], actual[i][j], tolerance);
            }
        }
    }

    @Test
    public void testCalculateCostMatrixCentroidMorePreviousPoints() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        ArrayList<Obj> previous = new ArrayList<>();
        Obj obj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(10,20,30);
        obj.addCoord(11,20,31);
        obj.addCoord(10,21,31);
        // Centroid @ [10.3333,20.3333,30.6667]
        previous.add(obj);

        obj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(43,30,25);
        previous.add(obj);

        obj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(12,25,30);
        previous.add(obj);

        obj = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(15,20,32);
        previous.add(obj);

        ArrayList<Obj> current = new ArrayList<>();
        obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(32,2,20);
        current.add(obj);

        obj = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(21,39,25);
        obj.addCoord(21,40,26);
        obj.addCoord(22,40,27);
        // Centroid @ [21.3333,39.6667,26]
        current.add(obj);

        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(22,3,10);
        current.add(obj);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        float[][] actual = trackObjects.calculateCostMatrix(previous,current,null);
        float[][] expected = new float[][]{
                {60.4154f,39.1152f,58.5577f,64.9076f},
                {32.2371f,24.2465f,26.4995f,36.4265f},
                {105.4247f,82.4318f,102.8786f,111.5258f}};

        assertEquals(3,actual.length);
        assertEquals(4,actual[1].length);

        for (int i=0;i<expected.length;i++) {
            for (int j=0;j<expected[i].length;j++) {
                assertEquals(expected[i][j], actual[i][j], tolerance);
            }
        }
    }

    @Test
    public void testCalculateCostMatrixCentroidMoreCurrentPoints() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        ArrayList<Obj> previous = new ArrayList<>();
        Obj obj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(10,20,30);
        obj.addCoord(11,20,31);
        obj.addCoord(10,21,31);
        previous.add(obj);

        obj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(43,30,25);
        previous.add(obj);

        obj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj.addCoord(12,25,30);
        previous.add(obj);

        ArrayList<Obj> current = new ArrayList<>();
        obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(32,2,20);
        current.add(obj);

        obj = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(21,39,25);
        obj.addCoord(21,40,26);
        obj.addCoord(22,40,27);
        current.add(obj);

        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(22,3,10);
        current.add(obj);

        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj.addCoord(20,14,12);
        current.add(obj);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        float[][] actual = trackObjects.calculateCostMatrix(previous,current,null);
        float[][] expected = new float[][]{
                {60.4154f,39.1152f,58.5577f},
                {32.2371f,24.2465f,26.4995f},
                {105.4247f,82.4318f,102.8786f},
                {94.0463f,70.7814f,91.0220f}};

        assertEquals(4,actual.length);
        assertEquals(3,actual[1].length);

        for (int i=0;i<expected.length;i++) {
            for (int j=0;j<expected[i].length;j++) {
                assertEquals(expected[i][j], actual[i][j], tolerance);
            }
        }
    }


    // TESTING calculateCostMatrix FOR OVERLAP MODE

    @Test @Ignore
    public void testCalculateCostMatrixOverlap() {
    }


    // TESTING testLinkValidity FOR CENTROID MODE

    @Test
    public void testTestLinkValidityOnMaxLinkingDistance() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj1.addCoord(43,30,25);

        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj2.addCoord(32,30,25);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,11d)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        assertTrue(trackObjects.testLinkValidity(obj1,obj2,null));

    }

    @Test
    public void testTestLinkValidityWithinMaxLinkingDistance() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj1.addCoord(43,30,25);

        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj2.addCoord(40,29,24);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,11d)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        assertTrue(trackObjects.testLinkValidity(obj1,obj2,null));

    }

    @Test
    public void testTestLinkValidityOutsideMaxLinkingDistance() {
        // Setting object parameters
        String inputObjectsName = "Spot";
        String trackObjectsName = "Track";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating the previous frame objects
        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
        obj1.addCoord(43,30,25);

        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
        obj2.addCoord(32,2,20);

        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,11d)
                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
                .updateParameterValue(TrackObjects.USE_VOLUME,false);

        assertFalse(trackObjects.testLinkValidity(obj1,obj2,null));

    }


    // TESTING linkObjects

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


    // TESTING createNewTrack

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


    // TESTING identifyLeading
    @Test @Ignore
    public void testIdentifyLeading() {
    }


    // TESTING OTHER METHODS

    @Test
    public void testGetTitle() {
        assertNotNull(new TrackObjects().getTitle());
    }

    @Test @Ignore
    public void testRun() {
        // We're going to need loads of these
    }
}