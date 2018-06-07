package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 10/09/2017.
 */
public class MeasureObjectCentroidTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new MeasureObjectCentroid().getTitle());

    }

    @Test
    public void calculateCentroidMean() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising MeasureObjectCentroid
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.initialiseParameters();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,inputObjectsName);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEAN);

        // Running MeasureObjectCentroid
        measureObjectCentroid.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:testObjects.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_X_MEAN.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEAN_X_PX).getValue();
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_Y_MEAN.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEAN_Y_PX).getValue();
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_Z_MEAN.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEAN_Z_SLICE).getValue();
            assertEquals(expected,actual,tolerance);

        }
    }

    @Test
    public void calculateCentroidMedian() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedObjects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising MeasureObjectCentroid
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid();
        measureObjectCentroid.initialiseParameters();
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,inputObjectsName);
        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.CENTROID_METHOD,MeasureObjectCentroid.Methods.MEDIAN);

        // Running MeasureObjectCentroid
        measureObjectCentroid.run(workspace);

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:testObjects.values()) {
            // Testing measurements
            double expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_X_MEDIAN.name()).getValue();
            double actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEDIAN_X_PX).getValue();
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_Y_MEDIAN.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEDIAN_Y_PX).getValue();
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(ExpectedObjects3D.Measures.EXP_Z_MEDIAN.name()).getValue();
            actual = testObject.getMeasurement(MeasureObjectCentroid.Measurements.MEDIAN_Z_SLICE).getValue();
            assertEquals(expected,actual,tolerance);

        }
    }
}