package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.IJ;
import ij.ImagePlus;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects3D;
import wbif.sjx.ModularImageAnalysis.ExpectedSpots3D;
import wbif.sjx.ModularImageAnalysis.Module.InputOutput.ImageSaver;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.net.URLDecoder;

import static org.junit.Assert.*;

public class GetLocalObjectRegionTest {

    @Test
    public void testGetTitle() {
        assertNotNull(new GetLocalObjectRegion().getTitle());
    }

    @Test @Ignore
    public void testRun() {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        String outputObjectsName = "Output_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new ExpectedSpots3D().getObjects(inputObjectsName,true,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        GetLocalObjectRegion getLocalObjectRegion = new GetLocalObjectRegion();
        getLocalObjectRegion.initialiseParameters();
        getLocalObjectRegion.updateParameterValue(GetLocalObjectRegion.INPUT_OBJECTS,inputObjectsName);
        getLocalObjectRegion.updateParameterValue(GetLocalObjectRegion.OUTPUT_OBJECTS,outputObjectsName);

//        public static final String LOCAL_RADIUS = "Local radius";
//        public static final String CALIBRATED_RADIUS = "Calibrated radius";
//        public static final String USE_MEASUREMENT = "Use measurement for radius";
//        public static final String MEASUREMENT_NAME = "Measurement name";

    }
}