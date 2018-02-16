package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.FilterImage;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectShape;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.common.Object.Point;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 07/12/2017.
 */
public class FilterObjectsTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new FilterImage().getTitle());
    }

    @Test
    public void testRunMeasurementsLargerThan() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new ExpectedObjects3D().getObjects("TestObj",true,dppXY,dppZ,calibratedUnits);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        FilterObjects filterObjects = new FilterObjects();
        filterObjects.updateParameterValue(FilterObjects.INPUT_OBJECTS,"TestObj");
        filterObjects.updateParameterValue(FilterObjects.FILTER_METHOD,FilterObjects.FilterMethods.MEASUREMENTS_LARGER_THAN);
        filterObjects.updateParameterValue(FilterObjects.MEASUREMENT, "N_VOXELS");
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_VALUE,200d);

        // Running the module
        filterObjects.run(workspace,false);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));


    }

    @Test @Ignore
    public void equalityTest() throws Exception {
        Obj ob1 = new Obj("Ob1",1,1,1,"");
        ArrayList<Point<Integer>> pointArrayList = new ArrayList<>();
        pointArrayList.add(new Point<>(1,1,1));
        pointArrayList.add(new Point<>(5,2,4));
        ob1.setPoints(pointArrayList);

        Obj ob2 = new Obj("Ob2",1,1,1,"");
        ArrayList<Point<Integer>> pointArrayList2 = new ArrayList<>();
        pointArrayList2.add(new Point<>(1,1,1));
        pointArrayList2.add(new Point<>(5,2,4));
        ob2.setPoints(pointArrayList2);

        System.out.println(ob1.getPoints()+"_"+ob1.getPoints().hashCode());
        System.out.println(ob2.getPoints()+"_"+ob2.getPoints().hashCode());
        assertEquals(ob1.getPoints(),ob2.getPoints());

    }
}