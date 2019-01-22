package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.ExpectedObjects;
import wbif.sjx.ModularImageAnalysis.ExpectedObjects.Spots3D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.common.Exceptions.IntegerOverflowException;

import static org.junit.Assert.*;

public class GetLocalObjectRegionTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() {
        assertNotNull(new GetLocalObjectRegion().getTitle());
    }

    @Test @Ignore
    public void testRun() throws IntegerOverflowException {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        String outputObjectsName = "Output_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Spots3D().getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
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