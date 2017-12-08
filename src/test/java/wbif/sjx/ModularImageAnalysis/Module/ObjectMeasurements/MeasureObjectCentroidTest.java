//package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;
//
//import org.junit.Test;
//import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
//import wbif.sjx.ModularImageAnalysis.Object.Obj;
//import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
//import wbif.sjx.ModularImageAnalysis.Object.Workspace;
//
//import java.util.HashMap;
//
//import static org.junit.Assert.*;
//
///**
// * Created by Stephen Cross on 10/09/2017.
// */
//public class MeasureObjectCentroidTest {
//    private double tolerance = 1E-2;
//
//    @Test
//    public void testGetTitle() throws Exception {
//        assertNotNull(new MeasureObjectCentroid().getTitle());
//
//    }
//
//    @Test
//    public void testRunMean() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting object parameters
//        String inputObjectsName = "Test_objects";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "um";
//
//        // Creating objects and adding to workspace
//        ObjCollection testObjects = ExpectedObjects3D.getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
//        workspace.addObjects(testObjects);
//
//        // Initialising MeasureObjectCentroid
//        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
//        measureObjectCentroid.initialiseParameters();
//        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,inputObjectsName);
//        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);
//
//        // Running MeasureObjectCentroid
//        measureObjectCentroid.run(workspace,false);
//
//        // Getting expected values
//        HashMap<Integer, HashMap<ExpectedObjects3D.Measures, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();
//
//        // Running through each object, checking it has the expected number of children and the expected value
//        for (Obj testObject:testObjects.values()) {
//            int nPoints = testObject.getPoints().size();
//
//            // Testing the number of measurements
//            assertEquals(3,testObject.getMeasurements().size());
//
//            // Testing measurements
//            double expected = (double) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.X_MEAN);
//            double actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEAN_X).getValue();
//            assertEquals(expected,actual,tolerance);
//
//            expected = (double) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.Y_MEAN);
//            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEAN_Y).getValue();
//            assertEquals(expected,actual,tolerance);
//
//            expected = (double) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.Z_MEAN);
//            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEAN_Z).getValue();
//            assertEquals(expected,actual,tolerance);
//
//        }
//    }
//
//    @Test
//    public void calculateCentroidMedian() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting object parameters
//        String inputObjectsName = "Test_objects";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "um";
//
//        // Creating objects and adding to workspace
//        ObjCollection testObjects = ExpectedObjects3D.getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
//        workspace.addObjects(testObjects);
//
//        // Initialising MeasureObjectCentroid
//        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
//        measureObjectCentroid.initialiseParameters();
//        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,inputObjectsName);
//        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEDIAN);
//
//        // Running MeasureObjectCentroid
//        measureObjectCentroid.run(workspace,false);
//
//        // Getting expected values
//        HashMap<Integer, HashMap<ExpectedObjects3D.Measures, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();
//
//        // Running through each object, checking it has the expected number of children and the expected value
//        for (Obj testObject:testObjects.values()) {
//            int nPoints = testObject.getPoints().size();
//
//            // Testing the number of measurements
//            assertEquals(3,testObject.getMeasurements().size());
//
//            // Testing measurements
//            double expected = (double) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.X_MEDIAN);
//            double actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEDIAN_X).getValue();
//            assertEquals(expected,actual,tolerance);
//
//            expected = (double) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.Y_MEDIAN);
//            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEDIAN_Y).getValue();
//            assertEquals(expected,actual,tolerance);
//
//            expected = (double) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.Z_MEDIAN);
//            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEDIAN_Z).getValue();
//            assertEquals(expected,actual,tolerance);
//
//        }
//    }
//}