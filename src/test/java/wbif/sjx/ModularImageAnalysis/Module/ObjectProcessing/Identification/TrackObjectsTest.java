//// TODO: Tests for with/without extra measures for centroid linking (volume and measurement weighting)
//
//package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;
//
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//import wbif.sjx.ModularImageAnalysis.Module.Module;
//import wbif.sjx.ModularImageAnalysis.Object.Measurement;
//import wbif.sjx.ModularImageAnalysis.Object.Obj;
//import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
//
//import java.util.ArrayList;
//
//import static org.junit.Assert.*;
//
//public class TrackObjectsTest {
//    private double tolerance = 1E-2;
//
//    @BeforeClass
//    public static void setVerbose() {
//        Module.setVerbose(true);
//    }
//
//
//    // Testing getCandidateObjects
//
//    @Test
//    public void testGetCandidateObjectsBothPresent() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating a collection of objects at different timepoints
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
//        objects.add(obj1);
//        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj2);
//        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj3);
//        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj4);
//        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj5);
//        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
//        objects.add(obj6);
//        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj7);
//        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj8);
//        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj9);
//        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj10);
//        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj11);
//        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj12);
//
//        // Getting candidate objects for timepoints 20 and 21
//        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,20,21);
//
//        // Checking the expected objects are there
//        ArrayList<Obj> previous = candidates[0];
//        assertEquals(2,previous.size());
//        assertTrue(previous.contains(obj3));
//        assertTrue(previous.contains(obj9));
//
//        ArrayList<Obj> current = candidates[1];
//        assertEquals(4,current.size());
//        assertTrue(current.contains(obj2));
//        assertTrue(current.contains(obj4));
//        assertTrue(current.contains(obj7));
//        assertTrue(current.contains(obj12));
//
//    }
//
//    @Test
//    public void testGetCandidateObjectsBothPresentLargeGap() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating a collection of objects at different timepoints
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
//        objects.add(obj1);
//        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj2);
//        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj3);
//        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj4);
//        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj5);
//        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
//        objects.add(obj6);
//        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj7);
//        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj8);
//        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj9);
//        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj10);
//        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj11);
//        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj12);
//
//        // Getting candidate objects for timepoints 20 and 21
//        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,7,21);
//
//        // Checking the expected objects are there
//        ArrayList<Obj> previous = candidates[0];
//        assertEquals(1,previous.size());
//        assertTrue(previous.contains(obj1));
//
//        ArrayList<Obj> current = candidates[1];
//        assertEquals(4,current.size());
//        assertTrue(current.contains(obj2));
//        assertTrue(current.contains(obj4));
//        assertTrue(current.contains(obj7));
//        assertTrue(current.contains(obj12));
//
//    }
//
//    @Test
//    public void testGetCandidateObjectsMissingT1() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating a collection of objects at different timepoints
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
//        objects.add(obj1);
//        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj2);
//        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(19);
//        objects.add(obj3);
//        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj4);
//        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj5);
//        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
//        objects.add(obj6);
//        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj7);
//        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj8);
//        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(19);
//        objects.add(obj9);
//        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj10);
//        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj11);
//        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj12);
//
//        // Getting candidate objects for timepoints 20 and 21
//        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,20,21);
//
//        // Checking the expected objects are there
//        ArrayList<Obj> previous = candidates[0];
//        assertEquals(0,previous.size());
//
//        ArrayList<Obj> current = candidates[1];
//        assertEquals(4,current.size());
//        assertTrue(current.contains(obj2));
//        assertTrue(current.contains(obj4));
//        assertTrue(current.contains(obj7));
//        assertTrue(current.contains(obj12));
//    }
//
//    @Test
//    public void testGetCandidateObjectsMissingT2() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating a collection of objects at different timepoints
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
//        objects.add(obj1);
//        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(22);
//        objects.add(obj2);
//        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj3);
//        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(22);
//        objects.add(obj4);
//        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj5);
//        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
//        objects.add(obj6);
//        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(22);
//        objects.add(obj7);
//        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj8);
//        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj9);
//        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj10);
//        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj11);
//        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(22);
//        objects.add(obj12);
//
//        // Getting candidate objects for timepoints 20 and 21
//        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,20,21);
//
//        // Checking the expected objects are there
//        ArrayList<Obj> previous = candidates[0];
//        assertEquals(2,previous.size());
//        assertTrue(previous.contains(obj3));
//        assertTrue(previous.contains(obj9));
//
//        ArrayList<Obj> current = candidates[1];
//        assertEquals(0,current.size());
//
//    }
//
//    @Test
//    public void testGetCandidateObjectsMissingBoth() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating a collection of objects at different timepoints
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(7);
//        objects.add(obj1);
//        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj2);
//        Obj obj3 = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj3);
//        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj4);
//        Obj obj5 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj5);
//        Obj obj6 = new Obj(inputObjectsName,9,dppXY,dppZ,calibratedUnits,false).setT(5);
//        objects.add(obj6);
//        Obj obj7 = new Obj(inputObjectsName,10,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj7);
//        Obj obj8 = new Obj(inputObjectsName,11,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj8);
//        Obj obj9 = new Obj(inputObjectsName,12,dppXY,dppZ,calibratedUnits,false).setT(20);
//        objects.add(obj9);
//        Obj obj10 = new Obj(inputObjectsName,15,dppXY,dppZ,calibratedUnits,false).setT(9);
//        objects.add(obj10);
//        Obj obj11 = new Obj(inputObjectsName,16,dppXY,dppZ,calibratedUnits,false).setT(3);
//        objects.add(obj11);
//        Obj obj12 = new Obj(inputObjectsName,14,dppXY,dppZ,calibratedUnits,false).setT(21);
//        objects.add(obj12);
//
//        // Getting candidate objects for timepoints 20 and 21
//        ArrayList<Obj>[] candidates = new TrackObjects().getCandidateObjects(objects,40,41);
//
//        // Checking the expected objects are there
//        ArrayList<Obj> previous = candidates[0];
//        assertEquals(0,previous.size());
//
//        ArrayList<Obj> current = candidates[1];
//        assertEquals(0,current.size());
//
//    }
//
//
//    // Testing getCentroidSeparationCost
//
//    @Test
//    public void testGetCentroidSeparation() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,20,30);
//        obj1.addCoord(11,20,31);
//        obj1.addCoord(10,21,31);
//        // Centroid @ [10.3333,20.3333,30.6667]
//
//        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj2.addCoord(43,30,25);
//
//        Obj obj3 = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj3.addCoord(12,25,30);
//
//        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj4.addCoord(32,2,20);
//
//        Obj obj5 = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj5.addCoord(21,39,25);
//        obj5.addCoord(21,40,26);
//        obj5.addCoord(22,40,27);
//        // Centroid @ [21.3333,39.6667,26]
//
//        Obj obj6 = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj6.addCoord(22,3,10);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        double actual = trackObjects.getCentroidSeparationCost(obj1,obj4);
//        double expected = 60.4154;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj1,obj5);
//        expected = 32.2371;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj1,obj6);
//        expected = 105.4247;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj2,obj4);
//        expected = 39.1152;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj2,obj5);
//        expected = 24.2465;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj2,obj6);
//        expected = 82.4318;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj3,obj4);
//        expected = 58.5577;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj3,obj5);
//        expected = 26.4995;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj3,obj6);
//        expected = 102.8786;
//        assertEquals(expected,actual, tolerance);
//
//    }
//
//    @Test
//    public void testGetCentroidSeparationOverlappingPoints() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,20,30);
//        obj1.addCoord(11,20,31);
//        obj1.addCoord(10,21,31);
//        // Centroid @ [10.3333,20.3333,30.6667]
//
//        Obj obj2 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj2.addCoord(43,30,25);
//
//        Obj obj3 = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj3.addCoord(12,25,30);
//
//        Obj obj4 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj4.addCoord(43,30,25);
//
//        Obj obj5 = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj5.addCoord(21,39,25);
//        obj5.addCoord(21,40,26);
//        obj5.addCoord(22,40,27);
//        // Centroid @ [21.3333,39.6667,26]
//
//        Obj obj6 = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj6.addCoord(12,25,30);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        double actual = trackObjects.getCentroidSeparationCost(obj1,obj4);
//        double expected = 44.3097;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj1,obj5);
//        expected = 32.2371;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj1,obj6);
//        expected = 5.9723;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj2,obj4);
//        expected = 0.0;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj2,obj5);
//        expected = 24.246;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj2,obj6);
//        expected = 40.1373;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj3,obj4);
//        expected = 40.1373;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj3,obj5);
//        expected = 26.4995;
//        assertEquals(expected,actual, tolerance);
//
//        actual = trackObjects.getCentroidSeparationCost(obj3,obj6);
//        expected = 0.0;
//        assertEquals(expected,actual, tolerance);
//    }
//
//    @Test @Ignore
//    public void testGetCentroidSeparationUseVolumeWeighting1() {
//
//    }
//
//    @Test @Ignore
//    public void testGetCentroidSeparationUseVolumeWeighting5() {
//
//    }
//
//    @Test @Ignore
//    public void testGetCentroidSeparationUseMeasurementWeighting1() {
//
//    }
//
//    @Test @Ignore
//    public void testGetCentroidSeparationUseMeasurementWeighting5() {
//
//    }
//
//
//    // Testing getPreviousStepDirectionCost (based on 8 random sets of coordinates)
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY1() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(10,20,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(43,30,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(12,25,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(172.3);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY2() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(20,20,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(3,10,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(32,25,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(176.88);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY3() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(81,91,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(13,91,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(63,10,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(121.69);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY4() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(28,55,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(96,96,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(16,97,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(148.19);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY5() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(96,49,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(80,14,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(42,92,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(129.46);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY6() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(79,96,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(66,4,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(85,93,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(175.98);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY7() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(68,76,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(74,39,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(66,17,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(29.19);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXY8() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(71,3,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(28,5,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(10,82,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(74.18);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetPreviousStepDirectionCostXYOverlapping() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevPrevObj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevPrevObj.addCoord(28,5,0);
//        prevPrevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,2));
//        inputObjects.add(prevPrevObj);
//
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(28,5,0);
//        prevObj.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,1));
//        inputObjects.add(prevObj);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(10,82,0);
//        inputObjects.add(currObj);
//
//        double actual = TrackObjects.getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
//        double expected = Math.toRadians(0);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//
//    // Testing getAbsoluteOrientationCost
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopRightOri142() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(3,10,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(32,25,0);
//
//        double orientation = 142;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(114.65);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopRightOri30() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(3,10,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(32,25,0);
//
//        double orientation = 30;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(2.65);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopRightOriMinus66() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(3,10,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(32,25,0);
//
//        double orientation = -66;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(93.35);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopRightOriMinus127() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(3,10,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(32,25,0);
//
//        double orientation = -127;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(154.35);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomRightOri142() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(13,91,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(63,10,0);
//
//        double orientation = 142;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(159.69);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomRightOri30() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(13,91,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(63,10,0);
//
//        double orientation = 30;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(88.31);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomRightOriMinus66() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(13,91,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(63,10,0);
//
//        double orientation = -66;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(7.69);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomRightOriMinus127() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(13,91,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(63,10,0);
//
//        double orientation = -127;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(68.69);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomLeftOri142() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(43,30,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(12,25,0);
//
//        double orientation = 142;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(47.17);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomLeftOri30() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(43,30,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(12,25,0);
//
//        double orientation = 30;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(159.16);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomLeftOriMinus66() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(43,30,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(12,25,0);
//
//        double orientation = -66;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(104.83);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomLeftOriMinus90() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(43,30,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(12,25,0);
//
//        double orientation = -90;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(80.83);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomLeftOriMinus127() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(43,30,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(12,25,0);
//
//        double orientation = -127;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(43.83);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostBottomLeftOriMinus178() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(43,30,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(12,25,0);
//
//        double orientation = -178;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(7.17);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopLeftOri142() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(80,14,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(42,92,0);
//
//        double orientation = 142;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(26.03);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopLeftOri30() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(80,14,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(42,92,0);
//
//        double orientation = 30;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(85.97);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopLeftOriMinus66() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(80,14,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(42,92,0);
//
//        double orientation = -66;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(178.03);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostTopLeftOriMinus127() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(80,14,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(42,92,0);
//
//        double orientation = -127;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(117.03);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostCoincident() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(80,14,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(90,14,0);
//
//        double orientation = 0;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(0);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetAbsoluteOrientationDirectionCostOpposite() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        ObjCollection inputObjects = new ObjCollection("Objects");
//
//        // Creating the previous frame objects
//        Obj prevObj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        prevObj.addCoord(80,14,0);
//
//        Obj currObj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        currObj.addCoord(70,14,0);
//
//        double orientation = 180;
//
//        double actual = TrackObjects.getAbsoluteOrientationCost(prevObj,currObj,orientation);
//        double expected = Math.toRadians(0);
//
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//
//    // Testing getAbsoluteOverlap
//
//    @Test @Ignore
//    public void testGetAbsoluteOverlap() {
//    }
//
//
//    // Testing calculateCostMatrix FOR CENTROID REGISTRATION_MODE
//
//    @Test
//    public void testCalculateCostMatrixCentroid() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        ArrayList<Obj> previous = new ArrayList<>();
//        Obj obj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(10,20,30);
//        obj.addCoord(11,20,31);
//        obj.addCoord(10,21,31);
//        previous.add(obj);
//
//        obj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(43,30,25);
//        previous.add(obj);
//
//        obj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(12,25,30);
//        previous.add(obj);
//
//        ArrayList<Obj> current = new ArrayList<>();
//        obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(32,2,20);
//        current.add(obj);
//
//        obj = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(21,39,25);
//        obj.addCoord(21,40,26);
//        obj.addCoord(22,40,27);
//        current.add(obj);
//
//        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(22,3,10);
//        current.add(obj);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,Double.MAX_VALUE)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        double[][] actual = trackObjects.calculateCostMatrix(previous,current,null,null);
//        float[][] expected = new float[][]{
//                {60.4154f,39.1152f,58.5577f},
//                {32.2371f,24.2465f,26.4995f},
//                {105.4247f,82.4318f,102.8786f}};
//
//        assertEquals(3,actual.length);
//        assertEquals(3,actual[1].length);
//
//        for (int i=0;i<expected.length;i++) {
//            for (int j=0;j<expected[i].length;j++) {
//                assertEquals(expected[i][j], actual[i][j], tolerance);
//            }
//        }
//    }
//
//    @Test
//    public void testCalculateCostMatrixCentroidMorePreviousPoints() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        ArrayList<Obj> previous = new ArrayList<>();
//        Obj obj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(10,20,30);
//        obj.addCoord(11,20,31);
//        obj.addCoord(10,21,31);
//        // Centroid @ [10.3333,20.3333,30.6667]
//        previous.add(obj);
//
//        obj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(43,30,25);
//        previous.add(obj);
//
//        obj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(12,25,30);
//        previous.add(obj);
//
//        obj = new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(15,20,32);
//        previous.add(obj);
//
//        ArrayList<Obj> current = new ArrayList<>();
//        obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(32,2,20);
//        current.add(obj);
//
//        obj = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(21,39,25);
//        obj.addCoord(21,40,26);
//        obj.addCoord(22,40,27);
//        // Centroid @ [21.3333,39.6667,26]
//        current.add(obj);
//
//        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(22,3,10);
//        current.add(obj);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,Double.MAX_VALUE)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        double[][] actual = trackObjects.calculateCostMatrix(previous,current,null,null);
//        float[][] expected = new float[][]{
//                {60.4154f,39.1152f,58.5577f,64.9076f},
//                {32.2371f,24.2465f,26.4995f,36.4265f},
//                {105.4247f,82.4318f,102.8786f,111.5258f}};
//
//        assertEquals(3,actual.length);
//        assertEquals(4,actual[1].length);
//
//        for (int i=0;i<expected.length;i++) {
//            for (int j=0;j<expected[i].length;j++) {
//                assertEquals(expected[i][j], actual[i][j], tolerance);
//            }
//        }
//    }
//
//    @Test
//    public void testCalculateCostMatrixCentroidMoreCurrentPoints() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        ArrayList<Obj> previous = new ArrayList<>();
//        Obj obj = new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(10,20,30);
//        obj.addCoord(11,20,31);
//        obj.addCoord(10,21,31);
//        previous.add(obj);
//
//        obj = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(43,30,25);
//        previous.add(obj);
//
//        obj = new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj.addCoord(12,25,30);
//        previous.add(obj);
//
//        ArrayList<Obj> current = new ArrayList<>();
//        obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(32,2,20);
//        current.add(obj);
//
//        obj = new Obj(inputObjectsName,6,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(21,39,25);
//        obj.addCoord(21,40,26);
//        obj.addCoord(22,40,27);
//        current.add(obj);
//
//        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(22,3,10);
//        current.add(obj);
//
//        obj = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj.addCoord(20,14,12);
//        current.add(obj);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,Double.MAX_VALUE)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        double[][] actual = trackObjects.calculateCostMatrix(previous,current,null,null);
//        float[][] expected = new float[][]{
//                {60.4154f,39.1152f,58.5577f},
//                {32.2371f,24.2465f,26.4995f},
//                {105.4247f,82.4318f,102.8786f},
//                {94.0463f,70.7814f,91.0220f}};
//
//        assertEquals(4,actual.length);
//        assertEquals(3,actual[1].length);
//
//        for (int i=0;i<expected.length;i++) {
//            for (int j=0;j<expected[i].length;j++) {
//                assertEquals(expected[i][j], actual[i][j], tolerance);
//            }
//        }
//    }
//
//
//    // Testing calculateCostMatrix FOR OVERLAP REGISTRATION_MODE
//
//    @Test @Ignore
//    public void testCalculateCostMatrixOverlap() {
//    }
//
//
//    // Testing testLinkValidity FOR CENTROID REGISTRATION_MODE
//
//    @Test
//    public void testTestLinkValidityOnMaxLinkingDistance() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(43,30,25);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(32,30,25);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,11d)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        assertTrue(trackObjects.testLinkValidity(obj1,obj2,null,null));
//
//    }
//
//    @Test
//    public void testTestLinkValidityWithinMaxLinkingDistance() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(43,30,25);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(40,29,24);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,11d)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        assertTrue(trackObjects.testLinkValidity(obj1,obj2,null,null));
//
//    }
//
//    @Test
//    public void testTestLinkValidityOutsideMaxLinkingDistance() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(43,30,25);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(32,2,20);
//
//        TrackObjects trackObjects = (TrackObjects) new TrackObjects()
//                .updateParameterValue(TrackObjects.LINKING_METHOD,TrackObjects.LinkingMethods.CENTROID)
//                .updateParameterValue(TrackObjects.MAXIMUM_LINKING_DISTANCE,11d)
//                .updateParameterValue(TrackObjects.USE_MEASUREMENT,false)
//                .updateParameterValue(TrackObjects.DIRECTION_WEIGHTING_MODE,TrackObjects.DirectionWeightingModes.NONE)
//                .updateParameterValue(TrackObjects.USE_VOLUME,false);
//
//        assertFalse(trackObjects.testLinkValidity(obj1,obj2,null,null));
//
//    }
//
//
//    // Testing instantaneous orientation
//
//    @Test
//    public void testGetInstantaneousOrientationRadsPrevFrameTopRight() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(12,14,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(15,18,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        double expected = Double.NaN;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = 1.1071;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = 0.9273;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsNextFrameTopRight() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(12,14,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(15,18,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        double expected = 1.1071;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = 0.9273;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = Double.NaN;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsBothFramesTopRight() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(12,14,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(15,18,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        double expected = 1.1071;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 1.0172;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 0.9273;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsPrevFrameBottomRight() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(12,7,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(17,4,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        double expected = Double.NaN;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = -0.9828;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = -0.5404;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsNextFrameBottomRight() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(12,7,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(17,4,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        double expected = -0.9828;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = -0.7616;
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = -0.5404;
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsBothFramesBottomRight() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(12,7,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(17,4,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        double expected = -0.9828;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = -0.5404;
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = Double.NaN;
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsPrevFrameBottomLeft() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(-3,2,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(-5,-7,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        double expected = Double.NaN;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = -2.5899;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = -1.7895;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsNextFrameBottomLeft() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(-3,2,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(-5,-7,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        double expected = -2.5899;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = -1.7895;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = Double.NaN;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsBothFramesBottomLeft() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(-3,2,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(-5,-7,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        double expected = -2.5899;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = -2.1897;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = -1.7895;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsPrevFrameTopLeft() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(6,12,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(-3,15,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        double expected = Double.NaN;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = 2.6779;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_PREV);
//        expected = 2.8198;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsNextFrameTopLeft() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(6,12,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(-3,15,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        double expected = 2.6779;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = 2.8198;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_NEXT);
//        expected = Double.NaN;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsBothFramesTopLeft() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,25);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(6,12,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(-3,15,25);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        double expected = 2.6779;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 2.7489;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 2.8198;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsBothFramesTopLeftDifferentZ() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,35);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(6,12,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(-3,15,15);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        double expected = 2.6779;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 2.7489;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 2.8198;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//    @Test
//    public void testGetInstantaneousOrientationRadsBothFramesNoMotion() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating ObjCollection
//        ObjCollection objects = new ObjCollection(inputObjectsName);
//
//        // Creating the previous frame objects
//        Obj obj1 = new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).setT(20);
//        obj1.addCoord(10,10,35);
//        obj1.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,5));
//        objects.add(obj1);
//
//        Obj obj2 = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false).setT(21);
//        obj2.addCoord(10,10,25);
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,2));
//        obj2.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_NEXT_ID,8));
//        objects.add(obj2);
//
//        Obj obj3 = new Obj(inputObjectsName,8,dppXY,dppZ,calibratedUnits,false).setT(22);
//        obj3.addCoord(10,10,15);
//        obj3.addMeasurement(new Measurement(TrackObjects.Measurements.TRACK_PREV_ID,5));
//        objects.add(obj3);
//
//        // Calculating the angle
//        double actual = TrackObjects.getInstantaneousOrientationRads(obj1,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        double expected = 0;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj2,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 0;
//        assertEquals(expected,actual,tolerance);
//
//        actual = TrackObjects.getInstantaneousOrientationRads(obj3,objects,TrackObjects.OrientationModes.RELATIVE_TO_BOTH);
//        expected = 0;
//        assertEquals(expected,actual,tolerance);
//
//    }
//
//
//    // Testing linkObjects
//
//    @Test
//    public void testLinkObjects() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating two objects and a track associated with the first object
//        Obj obj1 = new Obj(inputObjectsName,7,dppXY,dppZ,calibratedUnits,false);
//        Obj obj2 = new Obj(inputObjectsName,21,dppXY,dppZ,calibratedUnits,false);
//        Obj track = new Obj(trackObjectsName,4,dppXY,dppZ,calibratedUnits,false);
//        obj1.addParent(track);
//
//        // Creating the TrackObjects object
//        TrackObjects trackObjects = new TrackObjects();
//        trackObjects.initialiseParameters();
//        trackObjects.updateParameterValue(TrackObjects.TRACK_OBJECTS,trackObjectsName);
//
//        // Linking the objects
//        trackObjects.linkObjects(obj1,obj2);
//
//        // Checking the two objects are associated with each other
//        assertEquals(21,obj1.getMeasurement(TrackObjects.Measurements.TRACK_NEXT_ID).getValue(),tolerance);
//        assertNull(obj1.getMeasurement(TrackObjects.Measurements.TRACK_PREV_ID));
//        assertEquals(7,obj2.getMeasurement(TrackObjects.Measurements.TRACK_PREV_ID).getValue(),tolerance);
//        assertNull(obj2.getMeasurement(TrackObjects.Measurements.TRACK_NEXT_ID));
//
//        // Checking the second object inherited the track from the first
//        assertEquals(4,obj2.getParent(trackObjectsName).getID());
//
//    }
//
//
//    // Testing createNewTrack
//
//    @Test
//    public void testCreateNewTrack() {
//        // Setting object parameters
//        String inputObjectsName = "Spot";
//        String trackObjectsName = "Track";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "µm";
//
//        // Creating an object and the track objects collection for it to be added to
//        Obj obj = new Obj(inputObjectsName,5,dppXY,dppZ,calibratedUnits,false);
//        ObjCollection tracks = new ObjCollection(trackObjectsName);
//
//        // Creating the TrackObjects object
//        TrackObjects trackObjects = new TrackObjects();
//        trackObjects.initialiseParameters();
//
//        // Checking the track collection is empty
//        assertEquals(0,tracks.size());
//
//        // Creating the new track
//        trackObjects.createNewTrack(obj, tracks);
//
//        // Checking the track collection has increased by 1
//        assertEquals(1,tracks.size());
//
//        // Checking the input object is associated with the new track
//        assertEquals(5,tracks.getFirst().getChildren(inputObjectsName).getFirst().getID());
//        assertNotNull(obj.getParent(trackObjectsName));
//        assertNull(obj.getParent("fake name"));
//
//    }
//
//
//    // Testing identifyLeading
//    @Test @Ignore
//    public void testIdentifyLeading() {
//    }
//
//
//    // Testing OTHER METHODS
//
//    @Test
//    public void testGetTitle() {
//        assertNotNull(new TrackObjects().getTitle());
//    }
//
//    @Test @Ignore
//    public void testRun() {
//        // We're going to need loads of these
//    }
//}