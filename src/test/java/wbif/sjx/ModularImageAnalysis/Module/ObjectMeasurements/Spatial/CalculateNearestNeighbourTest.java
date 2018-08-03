package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import static org.junit.Assert.*;

public class CalculateNearestNeighbourTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() {
        assertNotNull(new CalculateNearestNeighbour().getTitle());
    }

    @Test
    public void testGetNearestNeighbour() {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1");

        Obj obj1 = new Obj("Objects 1",1,dppXY,dppZ,calibratedUnits,false);
        obj1.addCoord(10,20,40);
        objects1.add(obj1);

        Obj obj2 = new Obj("Objects 1",2,dppXY,dppZ,calibratedUnits,false);
        obj2.addCoord(20,30,10);
        objects1.add(obj2);

        Obj obj3 = new Obj("Objects 1",3,dppXY,dppZ,calibratedUnits,false);
        obj3.addCoord(20,20,30);
        objects1.add(obj3);

        Obj obj4 = new Obj("Objects 1",4,dppXY,dppZ,calibratedUnits,false);
        obj4.addCoord(50,20,10);
        objects1.add(obj4);

        // Testing against first object in set
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour();
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1,objects1,Double.MAX_VALUE);

        assertEquals(obj3,nearestNeighour);

    }

    @Test
    public void testGetNearestNeighbourOverlapping() {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1");

        Obj obj1 = new Obj("Objects 1",1,dppXY,dppZ,calibratedUnits,false);
        obj1.addCoord(10,20,40);
        objects1.add(obj1);

        Obj obj2 = new Obj("Objects 1",2,dppXY,dppZ,calibratedUnits,false);
        obj2.addCoord(20,30,10);
        objects1.add(obj2);

        Obj obj3 = new Obj("Objects 1",3,dppXY,dppZ,calibratedUnits,false);
        obj3.addCoord(20,20,30);
        objects1.add(obj3);

        Obj obj4 = new Obj("Objects 1",4,dppXY,dppZ,calibratedUnits,false);
        obj4.addCoord(50,20,10);
        objects1.add(obj4);

        Obj obj5 = new Obj("Objects 1",5,dppXY,dppZ,calibratedUnits,false);
        obj5.addCoord(10,20,40);
        objects1.add(obj5);

        // Testing against first object in set
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour();
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1,objects1,Double.MAX_VALUE);

        assertEquals(obj5,nearestNeighour);

    }

    @Test
    public void testGetNearestNeighbourLinkingDistanceNNFound() {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1");

        Obj obj1 = new Obj("Objects 1",1,dppXY,dppZ,calibratedUnits,false);
        obj1.addCoord(10,20,40);
        objects1.add(obj1);

        Obj obj2 = new Obj("Objects 1",2,dppXY,dppZ,calibratedUnits,false);
        obj2.addCoord(20,30,10);
        objects1.add(obj2);

        Obj obj3 = new Obj("Objects 1",3,dppXY,dppZ,calibratedUnits,false);
        obj3.addCoord(20,20,30);
        objects1.add(obj3);

        Obj obj4 = new Obj("Objects 1",4,dppXY,dppZ,calibratedUnits,false);
        obj4.addCoord(50,20,10);
        objects1.add(obj4);

        // Testing against first object in set
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour();
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1,objects1,100d);

        assertEquals(obj3,nearestNeighour);

    }

    @Test
    public void testGetNearestNeighbourLinkingDistanceNNNotFound() {
        // Setting object parameters
        String inputObjectsName = "Test_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection("Objects 1");

        Obj obj1 = new Obj("Objects 1",1,dppXY,dppZ,calibratedUnits,false);
        obj1.addCoord(10,20,40);
        objects1.add(obj1);

        Obj obj2 = new Obj("Objects 1",2,dppXY,dppZ,calibratedUnits,false);
        obj2.addCoord(20,30,10);
        objects1.add(obj2);

        Obj obj3 = new Obj("Objects 1",3,dppXY,dppZ,calibratedUnits,false);
        obj3.addCoord(20,20,30);
        objects1.add(obj3);

        Obj obj4 = new Obj("Objects 1",4,dppXY,dppZ,calibratedUnits,false);
        obj4.addCoord(50,20,10);
        objects1.add(obj4);

        // Testing against first object in set
        CalculateNearestNeighbour calculateNearestNeighbour = new CalculateNearestNeighbour();
        Obj nearestNeighour = calculateNearestNeighbour.getNearestNeighbour(obj1,objects1,50d);

        assertNull(nearestNeighour);

    }

    @Test
    public void testRunWithinSameSet() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);
        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);
        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);
        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);
        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,false)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,false)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,Double.MAX_VALUE)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(3,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(50.99,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(4,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(1,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(50.99,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(2,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunWithinSameSetMaxDistAllPass() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);

        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);

        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);

        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);

        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,false)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,true)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,100d)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);


//        // Creating second object set
//        ObjCollection objects2 = new ObjCollection("Objects 2");
//
//        Obj obj5 = new Obj("Objects 2",1,dppXY,dppZ,calibratedUnits,false);
//        obj5.addCoord(12,25,40);
//        objects2.add(obj5);
//
//        Obj obj6 = new Obj("Objects 2",2,dppXY,dppZ,calibratedUnits,false);
//        obj6.addCoord(20,35,10);
//        objects2.add(obj6);
//
//        Obj obj7 = new Obj("Objects 2",3,dppXY,dppZ,calibratedUnits,false);
//        obj7.addCoord(35,20,20);
//        objects2.add(obj7);


        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(3,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(50.99,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(4,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(1,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(50.99,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(2,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunWithinSameSetMaxDistSomeFail() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);

        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);

        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);

        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);

        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,false)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,true)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,50d)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);


//        // Creating second object set
//        ObjCollection objects2 = new ObjCollection("Objects 2");
//
//        Obj obj5 = new Obj("Objects 2",1,dppXY,dppZ,calibratedUnits,false);
//        obj5.addCoord(12,25,40);
//        objects2.add(obj5);
//
//        Obj obj6 = new Obj("Objects 2",2,dppXY,dppZ,calibratedUnits,false);
//        obj6.addCoord(20,35,10);
//        objects2.add(obj6);
//
//        Obj obj7 = new Obj("Objects 2",3,dppXY,dppZ,calibratedUnits,false);
//        obj7.addCoord(35,20,20);
//        objects2.add(obj7);


        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(4,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(2,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunWithinSameSetMaxDistCalibratedSomeFail() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);

        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);

        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);

        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);

        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,false)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,true)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,1d)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,true);

        // Run module
        calculateNearestNeighbour.run(workspace);


//        // Creating second object set
//        ObjCollection objects2 = new ObjCollection("Objects 2");
//
//        Obj obj5 = new Obj("Objects 2",1,dppXY,dppZ,calibratedUnits,false);
//        obj5.addCoord(12,25,40);
//        objects2.add(obj5);
//
//        Obj obj6 = new Obj("Objects 2",2,dppXY,dppZ,calibratedUnits,false);
//        obj6.addCoord(20,35,10);
//        objects2.add(obj6);
//
//        Obj obj7 = new Obj("Objects 2",3,dppXY,dppZ,calibratedUnits,false);
//        obj7.addCoord(35,20,20);
//        objects2.add(obj7);


        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(4,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(2,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunWithinSameSetWithinParent() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);
        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);
        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);
        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);
        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName);
        workspace.addObjects(parents);

        Obj parent1 = new Obj(parentObjectsName,1,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj4);
        obj4.addParent(parent1);

        Obj parent2 = new Obj(parentObjectsName,2,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);


        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,true)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,parentObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,false)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,Double.MAX_VALUE)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(2,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(150.67,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(4,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(2,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunWithinSameSetWithinParentMaxDistSomeFail() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);
        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);
        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);
        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);
        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName);
        workspace.addObjects(parents);

        Obj parent1 = new Obj(parentObjectsName,1,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj4);
        obj4.addParent(parent1);

        Obj parent2 = new Obj(parentObjectsName,2,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);


        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.WITHIN_SAME_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,true)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,parentObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,true)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,100d)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(4,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,inputObjectsName);
        assertEquals(2,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,inputObjectsName);
        assertEquals(31.62,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunDifferentSets() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);
        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);
        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);
        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);
        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName);
        workspace.addObjects(objects2);
        Obj obj5 = (Obj) new Obj(secondObjectsName,5,dppXY,dppZ,calibratedUnits,false).addCoord(12,25,40);
        objects2.add(obj5);
        Obj obj6 = (Obj) new Obj(secondObjectsName,6,dppXY,dppZ,calibratedUnits,false).addCoord(20,35,10);
        objects2.add(obj6);
        Obj obj7 = (Obj) new Obj(secondObjectsName,7,dppXY,dppZ,calibratedUnits,false).addCoord(35,20,20);
        objects2.add(obj7);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,secondObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,false)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,false)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,Double.MAX_VALUE)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(5,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(5.39,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(6,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(5,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(5,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(50.88,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(6,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(33.54,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunDifferentSetsMaxDistSomePass() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);
        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);
        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);
        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);
        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName);
        workspace.addObjects(objects2);
        Obj obj5 = (Obj) new Obj(secondObjectsName,5,dppXY,dppZ,calibratedUnits,false).addCoord(12,25,40);
        objects2.add(obj5);
        Obj obj6 = (Obj) new Obj(secondObjectsName,6,dppXY,dppZ,calibratedUnits,false).addCoord(20,35,10);
        objects2.add(obj6);
        Obj obj7 = (Obj) new Obj(secondObjectsName,7,dppXY,dppZ,calibratedUnits,false).addCoord(35,20,20);
        objects2.add(obj7);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,secondObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,false)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,null)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,true)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,50d)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(5,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(5.39,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(6,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(5,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertTrue(Double.isNaN(obj3.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(6,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(33.54,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunDifferentSetsWithinParent() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);
        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);
        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);
        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);
        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName);
        workspace.addObjects(objects2);
        Obj obj5 = (Obj) new Obj(secondObjectsName,5,dppXY,dppZ,calibratedUnits,false).addCoord(12,25,40);
        objects2.add(obj5);
        Obj obj6 = (Obj) new Obj(secondObjectsName,6,dppXY,dppZ,calibratedUnits,false).addCoord(20,35,10);
        objects2.add(obj6);
        Obj obj7 = (Obj) new Obj(secondObjectsName,7,dppXY,dppZ,calibratedUnits,false).addCoord(35,20,20);
        objects2.add(obj7);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName);
        workspace.addObjects(parents);

        Obj parent1 = new Obj(parentObjectsName,1,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj7);
        obj7.addParent(parent1);

        Obj parent2 = new Obj(parentObjectsName,2,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);
        parent2.addChild(obj4);
        obj4.addParent(parent2);
        parent2.addChild(obj5);
        obj5.addParent(parent2);
        parent2.addChild(obj6);
        obj6.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,secondObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,true)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,parentObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,false)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,Double.MAX_VALUE)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(7,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(103.08,obj1.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(7,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(53.15,obj2.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(5,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(50.88,obj3.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(6,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(33.54,obj4.getMeasurement(name).getValue(),tolerance);

    }

    @Test
    public void testRunDifferentSetsWithinParentMaxDistSomeFail() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Objects 1";
        String secondObjectsName = "Objects 2";
        String parentObjectsName = "Parents";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating first object set
        ObjCollection objects1 = new ObjCollection(inputObjectsName);
        workspace.addObjects(objects1);
        Obj obj1 = (Obj) new Obj(inputObjectsName,1,dppXY,dppZ,calibratedUnits,false).addCoord(10,20,40);
        objects1.add(obj1);
        Obj obj2 = (Obj) new Obj(inputObjectsName,2,dppXY,dppZ,calibratedUnits,false).addCoord(20,30,10);
        objects1.add(obj2);
        Obj obj3 = (Obj) new Obj(inputObjectsName,3,dppXY,dppZ,calibratedUnits,false).addCoord(20,20,30);
        objects1.add(obj3);
        Obj obj4 = (Obj) new Obj(inputObjectsName,4,dppXY,dppZ,calibratedUnits,false).addCoord(50,20,10);
        objects1.add(obj4);

        // Creating second object set
        ObjCollection objects2 = new ObjCollection(secondObjectsName);
        workspace.addObjects(objects2);
        Obj obj5 = (Obj) new Obj(secondObjectsName,5,dppXY,dppZ,calibratedUnits,false).addCoord(12,25,40);
        objects2.add(obj5);
        Obj obj6 = (Obj) new Obj(secondObjectsName,6,dppXY,dppZ,calibratedUnits,false).addCoord(20,35,10);
        objects2.add(obj6);
        Obj obj7 = (Obj) new Obj(secondObjectsName,7,dppXY,dppZ,calibratedUnits,false).addCoord(35,20,20);
        objects2.add(obj7);

        // Creating the parent object set
        ObjCollection parents = new ObjCollection(parentObjectsName);
        workspace.addObjects(parents);

        Obj parent1 = new Obj(parentObjectsName,1,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent1);
        parent1.addChild(obj1);
        obj1.addParent(parent1);
        parent1.addChild(obj2);
        obj2.addParent(parent1);
        parent1.addChild(obj7);
        obj7.addParent(parent1);

        Obj parent2 = new Obj(parentObjectsName,2,dppXY,dppZ,calibratedUnits,false);
        parents.add(parent2);
        parent2.addChild(obj3);
        obj3.addParent(parent2);
        parent2.addChild(obj4);
        obj4.addParent(parent2);
        parent2.addChild(obj5);
        obj5.addParent(parent2);
        parent2.addChild(obj6);
        obj6.addParent(parent2);

        // Initialising Module
        CalculateNearestNeighbour calculateNearestNeighbour = (CalculateNearestNeighbour) new CalculateNearestNeighbour()
                .updateParameterValue(CalculateNearestNeighbour.INPUT_OBJECTS,inputObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.RELATIONSHIP_MODE, CalculateNearestNeighbour.RelationshipModes.DIFFERENT_SET)
                .updateParameterValue(CalculateNearestNeighbour.NEIGHBOUR_OBJECTS,secondObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.CALCULATE_WITHIN_PARENT,true)
                .updateParameterValue(CalculateNearestNeighbour.PARENT_OBJECTS,parentObjectsName)
                .updateParameterValue(CalculateNearestNeighbour.LIMIT_LINKING_DISTANCE,true)
                .updateParameterValue(CalculateNearestNeighbour.MAXIMUM_LINKING_DISTANCE,50d)
                .updateParameterValue(CalculateNearestNeighbour.CALIBRATED_DISTANCE,false);

        // Run module
        calculateNearestNeighbour.run(workspace);

        // Testing against first object in set
        String name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertTrue(Double.isNaN(obj1.getMeasurement(name).getValue()));

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_ID,secondObjectsName);
        assertEquals(6,obj4.getMeasurement(name).getValue(),tolerance);

        name = CalculateNearestNeighbour.getFullName(CalculateNearestNeighbour.Measurements.NN_DISTANCE_PX,secondObjectsName);
        assertEquals(33.54,obj4.getMeasurement(name).getValue(),tolerance);

    }
}