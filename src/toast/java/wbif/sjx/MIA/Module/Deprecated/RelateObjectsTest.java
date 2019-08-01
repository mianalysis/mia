// TODO: Add calibrated distances to proximity relations

package wbif.sjx.MIA.Module.Deprecated;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.*;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Created by Stephen Cross on 10/09/2017.
 */
public class RelateObjectsTest extends ModuleTest {
    private double tolerance = 1E-2;

    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new RelateObjects(null).getDescription());

    }

    @Test
    public void testLinkMatchingIDsOneChild() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new Spots3D().getObjects(inputSpotsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.MATCHING_IDS);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Checking the workspace contains two object sets
        assertEquals("Number of ObjSets in Workspace",2,workspace.getObjects().size());

        // Checking the number of objects in the set
        assertNotNull(workspace.getObjectSet(inputObjectsName));
        assertEquals(8,workspace.getObjectSet(inputObjectsName).size());
        assertNotNull(workspace.getObjectSet(inputSpotsName));
        assertEquals(25,workspace.getObjectSet(inputSpotsName).size());

        // Running through each object, checking it has the expected number of children and the expected value
        for (Obj testObject:testObjects.values()) {
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

            // Testing the number of children
            assertNotNull("Object has spot children",childSpots);
            assertEquals("One child per parent",1,childSpots.size());

            // Testing spot for parent
            Obj childSpot = childSpots.values().iterator().next();
            assertNotNull("Child spot has parent",childSpot.getParent(inputObjectsName));

            // Testing spot is at expected location
            int nPoints = testObject.getPoints().size();
            double expected = testObject.getMeasurement(Objects3D.Measures.EXP_SPOT_ID_X.name()).getValue();
            double actual = childSpot.getX(true)[0];
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_SPOT_ID_Y.name()).getValue();
            actual = childSpot.getY(true)[0];
            assertEquals(expected,actual,tolerance);

            expected = testObject.getMeasurement(Objects3D.Measures.EXP_SPOT_ID_Z.name()).getValue();
            actual = childSpot.getZ(true,false)[0];
            assertEquals(expected,actual,tolerance);

        }
    }

    @Test
    public void testProximityCentroidLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new Spots3D().getObjects(inputSpotsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = new Objects3D().getOtherValues();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_X.name());
            int[] expectedY = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_Y.name());
            int[] expectedZ = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_Z.name());
            double[] expectedDist = (double[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_DIST.name());

            // Getting child objects (those linked here)
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

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
                String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,inputObjectsName);
                actualDist[iter++] = childSpot.getMeasurement(name).getValue();

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
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test_objects";
        String inputSpotsName = "Test_spots";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Objects3D().getObjects(inputObjectsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);
        ObjCollection testSpots = new Spots3D().getObjects(inputSpotsName,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testSpots);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,inputObjectsName);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,inputSpotsName);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,20.0);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Getting expected values
        HashMap<Integer, HashMap<String, Object>> expectedValues = new Objects3D().getOtherValues();

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj testObject:testObjects.values()) {
            // Getting expected values for this object
            HashMap<String, Object> currExpectedValues = expectedValues.get(testObject.getPoints().size());
            int[] expectedX = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_X.name());
            int[] expectedY = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_Y.name());
            int[] expectedZ = (int[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_Z.name());
            double[] expectedDist = (double[]) currExpectedValues.get(Objects3D.Measures.EXP_SPOT_PROX_CENT_20PX_DIST.name());

            // Getting child objects (those linked here)
            ObjCollection childSpots = testObject.getChildren(inputSpotsName);

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
                String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,inputObjectsName);
                actualDist[iter++] = childSpot.getMeasurement(name).getValue();

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
    public void testProximityCentroidResponse2D() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedCentDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENTROID_PX,proxObj1Name);
            double actualCentDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedCentDistPx, actualCentDistPx, tolerance);

        }
    }

    @Test
    public void testProximitySurfaceLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_ID.name()).getValue();
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID, tolerance);

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj2Name);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_CAL.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_CAL,proxObj2Name);
            double actualSurfDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @Test
    public void testProximitySurfaceLink5px() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,5.0);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_ID_5PX.name()).getValue();
            if (Double.isNaN(expectedParentID)) {
                assertNull(parentObj);
            } else {
                int actualParentID = parentObj.getID();
                assertEquals(expectedParentID, actualParentID, tolerance);
            }

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_PX_5PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj2Name);
            double actualSurfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

            double expectedSurfDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.SURF_PROX_DIST_CAL_5PX.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_CAL,proxObj2Name);
            double actualSurfDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistCal, actualSurfDistCal, tolerance);

        }
    }

    @Test
    public void testProximitySurfaceResponse2DInOut() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.SURF_PROX_DIST_PX_INOUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test
    public void testProximitySurfaceResponse2DIn() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.SURF_PROX_DIST_PX_IN.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test
    public void testProximitySurfaceResponse2DOut() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.OUTSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.SURF_PROX_DIST_PX_OUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_SURFACE_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceLink() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_ID.name()).getValue();
            int actualParentID = parentObj.getID();
            assertEquals(expectedParentID, actualParentID, tolerance);

            // Checking the distance to the parent
            double expectedDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj2Name);
            double actualfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistPx, actualfDistPx, tolerance);

            double expectedDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_CAL.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_CAL,proxObj2Name);
            double actualDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistCal, actualDistCal, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceLink5px() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxCubes1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxCubes2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,true);
        relateObjects.updateParameterValue(RelateObjects.LINKING_DISTANCE,5.0);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj1Obj:proxObj1.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj1Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the parent ID is the one expected
            Obj parentObj = proxObj1Obj.getParent(proxObj2Name);
            double expectedParentID = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_ID_5PX.name()).getValue();
            if (Double.isNaN(expectedParentID)) {
                assertNull(parentObj);
            } else {
                int actualParentID = parentObj.getID();
                assertEquals(expectedParentID, actualParentID, tolerance);
            }

            // Checking the distance to the parent
            double expectedDistPx = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_PX_5PX.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj2Name);
            double actualfDistPx = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistPx, actualfDistPx, tolerance);

            double expectedDistCal = proxObj1Obj.getMeasurement(ProxCubes1.Measures.CENT_SURF_PROX_DIST_CAL_5PX.name()).getValue();
            name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_CAL,proxObj2Name);
            double actualDistCal = proxObj1Obj.getMeasurement(name).getValue();
            assertEquals(expectedDistCal, actualDistCal, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceResponse2DInOut() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_SURF_PROX_DIST_PX_INOUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceResponse2DIn() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.INSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_SURF_PROX_DIST_PX_IN.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test
    public void testProximityCentroidSurfaceResponse2DOut() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String proxObj1Name = "Prox_obj_1";
        String proxObj2Name = "Prox_obj_2";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection proxObj1 = new ProxSquares1().getObjects(proxObj1Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj1);
        ObjCollection proxObj2 = new ProxSquares2().getObjects(proxObj2Name,ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(proxObj2);

        // Initialising RelateObjects
        RelateObjects relateObjects = new RelateObjects(null);
        
        relateObjects.updateParameterValue(RelateObjects.PARENT_OBJECTS,proxObj1Name);
        relateObjects.updateParameterValue(RelateObjects.CHILD_OBJECTS,proxObj2Name);
        relateObjects.updateParameterValue(RelateObjects.RELATE_MODE,RelateObjects.RelateModes.PROXIMITY);
        relateObjects.updateParameterValue(RelateObjects.REFERENCE_POINT,RelateObjects.ReferencePoints.CENTROID_TO_SURFACE);
        relateObjects.updateParameterValue(RelateObjects.LIMIT_LINKING_BY_DISTANCE,false);
        relateObjects.updateParameterValue(RelateObjects.INSIDE_OUTSIDE_MODE,RelateObjects.InsideOutsideModes.OUTSIDE_ONLY);

        // Running RelateObjects
        relateObjects.execute(workspace);

        // Running through each object, checking it has the expected number of measurements and the expected value
        for (Obj proxObj2Obj:proxObj2.values()) {
            // Checking the object has no children
            LinkedHashMap<String, ObjCollection> children = proxObj2Obj.getChildren();
            assertEquals(0, children.size());

            // Checking the distance to the parent
            double expectedSurfDistPx = proxObj2Obj.getMeasurement(ProxSquares2.Measures.CENT_SURF_PROX_DIST_PX_OUT.name()).getValue();
            String name = RelateObjects.getFullName(RelateObjects.Measurements.DIST_CENT_SURF_PX,proxObj1Name);
            double actualSurfDistPx = proxObj2Obj.getMeasurement(name).getValue();
            assertEquals(expectedSurfDistPx, actualSurfDistPx, tolerance);

        }
    }

    @Test @Ignore
    public void testProximityToChildren() throws Exception {
    }

    @Test @Ignore
    public void testSpatialLinking() throws Exception {
    }

}