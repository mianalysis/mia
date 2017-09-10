// TODO: Add calibrated distances to proximity relations

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedSpots3D;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjSet;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 10/09/2017.
 */
public class RelateObjectsTest {
    private double tolerance = 1E-2;

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new RelateObjects().getTitle());

    }

    @Test
    public void testLinkMatchingIDsOneChild() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjSet testObjects = ExpectedObjects3D.getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);
        ObjSet testSpots = ExpectedSpots3D.getObjects(inputSpotsName,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.MATCHING_IDS);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Checking the workspace contains two object sets
        assertEquals("Number of ObjSets in Workspace",2,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());
        assertNotNull(workspace.getObjectSet(inputSpotsName));
        assertEquals(25,workspace.getObjectSet(inputSpotsName).size());

        // Getting expected values
        HashMap<Integer,HashMap<ExpectedObjects3D.Measures,Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:testObjects.values()) {
            ObjSet childSpots = testObject.getChildren(inputSpotsName);

            // Testing the number of children
            assertNotNull("Object has spot children",childSpots);
            assertEquals("One child per parent",1,childSpots.size());

            // Testing spot for parent
            Obj childSpot = childSpots.values().iterator().next();
            assertNotNull("Child spot has parent",childSpot.getParent(inputObjectsName));

            // Testing spot is at expected location
            int nPoints = testObject.getPoints().size();
            int expected = (int) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.SPOT_ID_X);
            int actual = (int) childSpot.getX(true)[0];
            assertEquals(expected,actual);

            expected = (int) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.SPOT_ID_Y);
            actual = (int) childSpot.getY(true)[0];
            assertEquals(expected,actual);

            expected = (int) expectedValues.get(nPoints).get(ExpectedObjects3D.Measures.SPOT_ID_Z);
            actual = (int) childSpot.getZ(true,false)[0];
            assertEquals(expected,actual);

        }
    }

    @Test
    public void testProximityCentroidLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjSet testObjects = ExpectedObjects3D.getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);
        ObjSet testSpots = ExpectedSpots3D.getObjects(inputSpotsName,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,Double.MAX_VALUE);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Getting expected values
        HashMap<Integer, HashMap<ExpectedObjects3D.Measures, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<ExpectedObjects3D.Measures, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_X);
            int[] expectedY = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_Y);
            int[] expectedZ = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_Z);
            double[] expectedDist = (double[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_DIST);

            // Getting child objects (those linked here)
            ObjSet childSpots = testObject.getChildren(inputSpotsName);

            // Each object won't necessarily have any children
            if (childSpots == null) continue;

            // Testing the number of children
            assertEquals("Number of children",expectedX.length,childSpots.size());

            // Putting actual values into arrays
            int[] actualX = new int[childSpots.size()];
            int[] actualY = new int[childSpots.size()];
            int[] actualZ = new int[childSpots.size()];
            double[] actualDist = new double[childSpots.size()];

            int iter = 0;
            for (Obj childSpot:childSpots.values()) {
                actualX[iter] = (int) childSpot.getX(true)[0];
                actualY[iter] = (int) childSpot.getY(true)[0];
                actualZ[iter] = (int) childSpot.getZ(true,false)[0];
                actualDist[iter++] = childSpot.getMeasurement("Distance to parent centroid (px)").getValue();

            }

            // Sorting arrays
            Arrays.sort(expectedX);
            Arrays.sort(expectedY);
            Arrays.sort(expectedZ);
            Arrays.sort(expectedDist);
            Arrays.sort(actualX);
            Arrays.sort(actualY);
            Arrays.sort(actualZ);
            Arrays.sort(actualDist);

            // Comparing arrays
            assertArrayEquals(expectedX,actualX);
            assertArrayEquals(expectedY,actualY);
            assertArrayEquals(expectedZ,actualZ);
            assertArrayEquals(expectedDist,actualDist,tolerance);

        }
    }

    @Test
    public void testProximityCentroidLink20px() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "um";

        // Creating objects and adding to workspace
        ObjSet testObjects = ExpectedObjects3D.getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);
        ObjSet testSpots = ExpectedSpots3D.getObjects(inputSpotsName,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects();
        relateObjects.initialiseParameters();
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,20.0);

        // Running RelateObjects
        relateObjects.run(workspace,false);

        // Getting expected values
        HashMap<Integer, HashMap<ExpectedObjects3D.Measures, Object>> expectedValues = ExpectedObjects3D.getExpectedValues3D();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<ExpectedObjects3D.Measures, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_X);
            int[] expectedY = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_Y);
            int[] expectedZ = (int[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_Z);
            double[] expectedDist = (double[]) currExpectedValues.get(ExpectedObjects3D.Measures.SPOT_PROX_CENT_20PX_DIST);

            // Getting child objects (those linked here)
            ObjSet childSpots = testObject.getChildren(inputSpotsName);

            // Each object won't necessarily have any children
            if (childSpots == null) continue;

            // Testing the number of children
            assertEquals("Number of children",expectedX.length,childSpots.size());

            // Putting actual values into arrays
            int[] actualX = new int[childSpots.size()];
            int[] actualY = new int[childSpots.size()];
            int[] actualZ = new int[childSpots.size()];
            double[] actualDist = new double[childSpots.size()];

            int iter = 0;
            for (Obj childSpot:childSpots.values()) {
                actualX[iter] = (int) childSpot.getX(true)[0];
                actualY[iter] = (int) childSpot.getY(true)[0];
                actualZ[iter] = (int) childSpot.getZ(true,false)[0];
                actualDist[iter++] = childSpot.getMeasurement("Distance to parent centroid (px)").getValue();

            }

            // Sorting arrays
            Arrays.sort(expectedX);
            Arrays.sort(expectedY);
            Arrays.sort(expectedZ);
            Arrays.sort(expectedDist);
            Arrays.sort(actualX);
            Arrays.sort(actualY);
            Arrays.sort(actualZ);
            Arrays.sort(actualDist);

            // Comparing arrays
            assertArrayEquals(expectedX,actualX);
            assertArrayEquals(expectedY,actualY);
            assertArrayEquals(expectedZ,actualZ);
            assertArrayEquals(expectedDist,actualDist,tolerance);

        }
    }

    @Test @Ignore
    public void testProximitySurfaceLink() throws Exception {

    }

    @Test @Ignore
    public void testProximitySurfaceLink20px() throws Exception {

    }

    @Test @Ignore
    public void testProximityToChildren() throws Exception {
    }

    @Test @Ignore
    public void testSpatialLinking() throws Exception {
    }
}