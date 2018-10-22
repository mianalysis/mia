package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import static org.junit.Assert.*;

public class MeasureObjectOverlapTest {

    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new MeasureObjectOverlap().getTitle());
    }

    @Test
    public void testGetNOverlappingPointsNoOverlap() throws Exception {
        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(20,22,32);
        object2_2.addCoord(20,21,32);
        object2_2.addCoord(20,22,33);
        object2_2.addCoord(19,22,32);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(10,22,32);
        object2_3.addCoord(10,21,32);
        object2_3.addCoord(10,22,33);
        object2_3.addCoord(9,22,32);
        objects2.add(object2_3);

        int actual = MeasureObjectOverlap.getNOverlappingPoints(object1_1,objects1,objects2,false);
        int expected = 0;

        assertEquals(expected,actual);

    }

    @Test
    public void testGetNOverlappingPointsPartialSingleObjectOverlap() throws Exception {
        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(10,22,32);
        object2_3.addCoord(10,21,32);
        object2_3.addCoord(10,22,33);
        object2_3.addCoord(9,22,32);
        objects2.add(object2_3);

        int actual = MeasureObjectOverlap.getNOverlappingPoints(object1_1,objects1,objects2,false);
        int expected = 4;

        assertEquals(expected,actual);

    }

    @Test
    public void testGetNOverlappingPointsPartialMultipleObjectOverlap() throws Exception {
        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(9,13,33);
        object2_3.addCoord(11,12,33);
        object2_3.addCoord(11,13,34);
        object2_3.addCoord(10,12,34);
        object2_3.addCoord(10,12,35);
        objects2.add(object2_3);

        int actual = MeasureObjectOverlap.getNOverlappingPoints(object1_1,objects1,objects2,false);
        int expected = 7;

        assertEquals(expected,actual);

    }

    /**
     * In this test, two of the test objects share the same pixel.  This shouldn't lead to an increase in the overlap
     * volume of the main object.
     * @throws Exception
     */
    @Test
    public void testGetNOverlappingPointsPartialMultipleObjectOverlapWithInternalClash() throws Exception {
        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(9,13,33);
        object2_3.addCoord(10,13,33);
        object2_3.addCoord(11,12,33);
        object2_3.addCoord(11,13,34);
        object2_3.addCoord(10,12,34);
        object2_3.addCoord(10,12,35);
        objects2.add(object2_3);

        int actual = MeasureObjectOverlap.getNOverlappingPoints(object1_1,objects1,objects2,false);
        int expected = 7;

        assertEquals(expected,actual);

    }

    @Test
    public void testGetNOverlappingPointsTotalOverlap() throws Exception {
// Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(10,12,32);
        object2_2.addCoord(11,12,32);
        object2_2.addCoord(10,13,32);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(9,13,33);
        object2_3.addCoord(10,13,33);
        object2_3.addCoord(11,12,33);
        object2_3.addCoord(11,13,34);
        object2_3.addCoord(10,12,34);
        object2_3.addCoord(10,12,35);
        objects2.add(object2_3);

        int actual = MeasureObjectOverlap.getNOverlappingPoints(object1_1,objects1,objects2,false);
        int expected = 10;

        assertEquals(expected,actual);
    }

    @Test
    public void testRunNoOverlap() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(20,22,32);
        object2_2.addCoord(20,21,32);
        object2_2.addCoord(20,22,33);
        object2_2.addCoord(19,22,32);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(10,22,32);
        object2_3.addCoord(10,21,32);
        object2_3.addCoord(10,22,33);
        object2_3.addCoord(9,22,32);
        objects2.add(object2_3);

        workspace.addObjects(objects1);
        workspace.addObjects(objects2);

        // Initialising MeasureObjectOverlap
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap();
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objectsName1);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objectsName2);

        // Running MeasureObjectOverlap
        measureObjectOverlap.run(workspace);

        // Getting the measurement for each object and checking it is as expected
        String measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_1);
        double actual = object1_1.getMeasurement(measurementName).getValue();
        double expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_VOX_1);
        actual = object1_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testRunPartialSingleObjectOverlap() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(10,22,32);
        object2_3.addCoord(10,21,32);
        object2_3.addCoord(10,22,33);
        object2_3.addCoord(9,22,32);
        objects2.add(object2_3);

        workspace.addObjects(objects1);
        workspace.addObjects(objects2);

        // Initialising MeasureObjectOverlap
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap();
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objectsName1);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objectsName2);

        // Running MeasureObjectOverlap
        measureObjectOverlap.run(workspace);

        // Getting the measurement for each object and checking it is as expected
        String measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_1);
        double actual = object1_1.getMeasurement(measurementName).getValue();
        double expected = 40;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_VOX_1);
        actual = object1_1.getMeasurement(measurementName).getValue();
        expected = 4;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 57.14;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 4;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testRunPartialMultipleObjectOverlap() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(9,13,33);
        object2_3.addCoord(11,12,33);
        object2_3.addCoord(11,13,34);
        object2_3.addCoord(10,12,34);
        object2_3.addCoord(10,12,35);
        objects2.add(object2_3);

        workspace.addObjects(objects1);
        workspace.addObjects(objects2);

        // Initialising MeasureObjectOverlap
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap();
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objectsName1);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objectsName2);

        // Running MeasureObjectOverlap
        measureObjectOverlap.run(workspace);

        // Getting the measurement for each object and checking it is as expected
        String measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_1);
        double actual = object1_1.getMeasurement(measurementName).getValue();
        double expected = 70;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_VOX_1);
        actual = object1_1.getMeasurement(measurementName).getValue();
        expected = 7;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 57.14;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 4;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 60;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 3;
        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testRunPartialMultipleObjectOverlapWithInternalClash() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(9,13,33);
        object2_3.addCoord(10,13,33);
        object2_3.addCoord(11,12,33);
        object2_3.addCoord(11,13,34);
        object2_3.addCoord(10,12,34);
        object2_3.addCoord(10,12,35);
        objects2.add(object2_3);

        workspace.addObjects(objects1);
        workspace.addObjects(objects2);

        // Initialising MeasureObjectOverlap
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap();
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objectsName1);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objectsName2);

        // Running MeasureObjectOverlap
        measureObjectOverlap.run(workspace);

        // Getting the measurement for each object and checking it is as expected
        String measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_1);
        double actual = object1_1.getMeasurement(measurementName).getValue();
        double expected = 70;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_VOX_1);
        actual = object1_1.getMeasurement(measurementName).getValue();
        expected = 7;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 57.14;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 4;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 66.67;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 4;
        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testRunTotalOverlap() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.addCoord(10,12,32);
        object2_2.addCoord(11,12,32);
        object2_2.addCoord(10,13,32);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.addCoord(9,13,33);
        object2_3.addCoord(10,13,33);
        object2_3.addCoord(11,12,33);
        object2_3.addCoord(11,13,34);
        object2_3.addCoord(10,12,34);
        object2_3.addCoord(10,12,35);
        objects2.add(object2_3);

        workspace.addObjects(objects1);
        workspace.addObjects(objects2);

        // Initialising MeasureObjectOverlap
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap();
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objectsName1);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objectsName2);

        // Running MeasureObjectOverlap
        measureObjectOverlap.run(workspace);

        // Getting the measurement for each object and checking it is as expected
        String measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_1);
        double actual = object1_1.getMeasurement(measurementName).getValue();
        double expected = 100;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_VOX_1);
        actual = object1_1.getMeasurement(measurementName).getValue();
        expected = 10;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 70;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 7;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 66.67;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 4;
        assertEquals(expected,actual,tolerance);

    }

    @Test
    public void testRunPartialMultipleObjectOverlapMultipleTimepoints() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String objectsName1 = "Test objects 1";
        String objectsName2 = "Test objects 2";

        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating a single test object
        ObjCollection objects1 = new ObjCollection(objectsName1);
        Obj object1_1 = new Obj(objectsName1,1,dppXY,dppZ,calibratedUnits,false);
        object1_1.setT(2);
        object1_1.addCoord(10,12,32);
        object1_1.addCoord(11,12,32);
        object1_1.addCoord(10,13,32);
        object1_1.addCoord(10,14,32);
        object1_1.addCoord(11,13,32);
        object1_1.addCoord(10,12,33);
        object1_1.addCoord(10,13,33);
        object1_1.addCoord(11,12,33);
        object1_1.addCoord(11,13,34);
        object1_1.addCoord(10,12,34);
        objects1.add(object1_1);

        // Creating a collection of multiple objects to test against
        ObjCollection objects2 = new ObjCollection(objectsName2);
        Obj object2_1 = new Obj(objectsName2,1,dppXY,dppZ,calibratedUnits,false);
        object2_1.setT(2);
        object2_1.addCoord(20,12,32);
        object2_1.addCoord(20,11,32);
        object2_1.addCoord(20,12,33);
        object2_1.addCoord(19,12,32);
        objects2.add(object2_1);

        Obj object2_2 = new Obj(objectsName2,2,dppXY,dppZ,calibratedUnits,false);
        object2_2.setT(3);
        object2_2.addCoord(9,13,32);
        object2_2.addCoord(9,14,32);
        object2_2.addCoord(10,14,32);
        object2_2.addCoord(11,13,32);
        object2_2.addCoord(10,12,33);
        object2_2.addCoord(10,13,33);
        object2_2.addCoord(9,13,33);
        objects2.add(object2_2);

        Obj object2_3 = new Obj(objectsName2,3,dppXY,dppZ,calibratedUnits,false);
        object2_3.setT(2);
        object2_3.addCoord(9,13,33);
        object2_3.addCoord(11,12,33);
        object2_3.addCoord(11,13,34);
        object2_3.addCoord(10,12,34);
        object2_3.addCoord(10,12,35);
        objects2.add(object2_3);

        workspace.addObjects(objects1);
        workspace.addObjects(objects2);

        // Initialising MeasureObjectOverlap
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap();
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objectsName1);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objectsName2);

        // Running MeasureObjectOverlap
        measureObjectOverlap.run(workspace);

        // Getting the measurement for each object and checking it is as expected
        String measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_1);
        double actual = object1_1.getMeasurement(measurementName).getValue();
        double expected = 30;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName2,MeasureObjectOverlap.Measurements.OVERLAP_VOX_1);
        actual = object1_1.getMeasurement(measurementName).getValue();
        expected = 3;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_1.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_2.getMeasurement(measurementName).getValue();
        expected = 0;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_PERCENT_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 60;
        assertEquals(expected,actual,tolerance);

        measurementName = MeasureObjectOverlap.getFullName(objectsName1,MeasureObjectOverlap.Measurements.OVERLAP_VOX_2);
        actual = object2_3.getMeasurement(measurementName).getValue();
        expected = 3;
        assertEquals(expected,actual,tolerance);

    }
}