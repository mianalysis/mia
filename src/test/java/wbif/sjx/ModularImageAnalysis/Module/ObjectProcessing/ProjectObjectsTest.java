//package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;
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
// * Created by Stephen Cross on 03/09/2017.
// */
//public class ProjectObjectsTest {
//    private double tolerance = 1E-2;
//
//    @Test
//    public void testGetTitle() throws Exception {
//        assertNotNull(new ProjectObjects().getTitle());
//
//    }
//
//    @Test
//    public void testRun() throws Exception {
//        // Creating a new workspace
//        Workspace workspace = new Workspace(0,null);
//
//        // Setting object parameters
//        String inputObjectsName = "Input objects";
//        String outputObjectsName = "Output objects";
//        double dppXY = 0.02;
//        double dppZ = 0.1;
//        String calibratedUnits = "um";
//
//        // Creating objects and adding to workspace
//        ObjCollection inputObjects = new ExpectedObjects3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits);
//        workspace.addObjects(inputObjects);
//
//        // Initialising ProjectObjects
//        ProjectObjects projectObjects = new ProjectObjects();
//        projectObjects.initialiseParameters();
//        projectObjects.updateParameterValue(ProjectObjects.INPUT_OBJECTS,inputObjectsName);
//        projectObjects.updateParameterValue(ProjectObjects.OUTPUT_OBJECTS,outputObjectsName);
//
//        // Running ProjectObjects
//        projectObjects.run(workspace,false);
//
//        // Testing there are now 2 sets of objects in the workspace and they have the expected names
//        assertEquals(2,workspace.getObjects().size());
//        assertNotNull(workspace.getObjectSet(inputObjectsName));
//        assertNotNull(workspace.getObjectSet(outputObjectsName));
//
//        // Testing number of objects in projected set
//        assertEquals(8,workspace.getObjectSet(outputObjectsName).size());
//
//        // Getting expected values
//        HashMap<Integer, HashMap<ExpectedObjects3D.Measures, Object>> expectedValues = ExpectedObjects3D.getMeasurements();
//
//        // Testing coordinate range for projected objects.  These are accessed via the number of voxels of the parent
//        // (as this is how they are stored in the expected values HashMap)
//        ObjCollection testObjects = workspace.getObjectSet(outputObjectsName);
//        for (Obj testObject:testObjects.values()) {
//            // Checking the objects have a single parent object and that parent has the expected number of voxels
//            assertEquals("Number of parents",1,testObject.getParents(true).size());
//            assertEquals("Number of parents",1,testObject.getParents(false).size());
//            assertNotNull("Correct parent",testObject.getParent(inputObjectsName));
//            assertEquals("Number of children",0,testObject.getChildren().size());
//
//            // Getting the parent object
//            Obj parentObject = testObject.getParent(inputObjectsName);
//
//            // Checking the number of children and parents for the parent object
//            assertEquals("Number of parents",0,parentObject.getParents(true).size());
//            assertEquals("Number of parents",0,parentObject.getParents(false).size());
//            assertEquals("Number of children",1,parentObject.getChildren().size());
//
//            // Getting the number of voxels in this object (this is used as the key for the expected values map)
//            int nVoxels = parentObject.getNVoxels();
//
//            // Getting the relevant measures
//            HashMap<ExpectedObjects3D.Measures, Object> expected = expectedValues.get(nVoxels);
//            assertNotNull("Null means no expected object with the specified number of voxels",expected);
//
//            // Testing coordinate ranges
//            int[][] coordinateRange = testObject.getCoordinateRange();
//            assertEquals("X-min",(int) expected.get(ExpectedObjects3D.Measures.X_MIN),coordinateRange[0][0],tolerance);
//            assertEquals("X-max",(int) expected.get(ExpectedObjects3D.Measures.X_MAX),coordinateRange[0][1],tolerance);
//            assertEquals("Y-min",(int) expected.get(ExpectedObjects3D.Measures.Y_MIN),coordinateRange[1][0],tolerance);
//            assertEquals("Y-max",(int) expected.get(ExpectedObjects3D.Measures.Y_MAX),coordinateRange[1][1],tolerance);
//              assertEquals("Z-min",0,0);
//            assertEquals("Z-max",0,0,tolerance);
//            assertEquals("F",(int) expected.get(ExpectedObjects3D.Measures.F),testObject.getT(),tolerance);
//            // Testing the number of voxels in the test object
//            int expectedNVoxels = (int) expected.get(ExpectedObjects3D.Measures.N_VOXELS_PROJ);
//            int actualNVoxels = testObject.getPoints().size();
//            assertEquals("Number of voxels", expectedNVoxels, actualNVoxels);
//
//            // Checking the objects have the correct spatial calibration
//            assertEquals("Spatial calibration in XY",dppXY,testObject.getDistPerPxXY(),tolerance);
//            assertEquals("Spatial calibration in Z",dppZ,testObject.getDistPerPxZ(),tolerance);
//
//        }
//    }
//}