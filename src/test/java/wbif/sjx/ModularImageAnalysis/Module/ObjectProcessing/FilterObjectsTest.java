package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.FilterImage;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

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
        filterObjects.updateParameterValue(FilterObjects.MEASUREMENT,ExpectedObjects3D.Measures.N_VOXELS);
        filterObjects.updateParameterValue(FilterObjects.REFERENCE_VALUE,200d);

        // Running the module
        filterObjects.run(workspace,false);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));


    }
}