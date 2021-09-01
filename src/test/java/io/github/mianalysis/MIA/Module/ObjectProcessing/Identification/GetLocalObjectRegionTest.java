package io.github.mianalysis.MIA.Module.ObjectProcessing.Identification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.MIA.ExpectedObjects.ExpectedObjects;
import io.github.mianalysis.MIA.ExpectedObjects.Spots3D;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.WorkspaceCollection;
import io.github.sjcross.common.Exceptions.IntegerOverflowException;
import io.github.sjcross.common.Object.Volume.VolumeType;

public class GetLocalObjectRegionTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new GetLocalObjectRegion(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    @Disabled
    public void testRun(VolumeType volumeType) throws IntegerOverflowException {
        // Creating a new workspace
        WorkspaceCollection workspaces = new WorkspaceCollection();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        String outputObjectsName = "Output_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        ObjCollection testObjects = new Spots3D(volumeType).getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Initialising FilterObjects module
        GetLocalObjectRegion getLocalObjectRegion = new GetLocalObjectRegion(null);
        getLocalObjectRegion.initialiseParameters();
        getLocalObjectRegion.updateParameterValue(GetLocalObjectRegion.INPUT_OBJECTS,inputObjectsName);
        getLocalObjectRegion.updateParameterValue(GetLocalObjectRegion.OUTPUT_OBJECTS,outputObjectsName);

//        public static final String LOCAL_RADIUS = "Local radius";
//        public static final String CALIBRATED_RADIUS = "Calibrated radius";
//        public static final String USE_MEASUREMENT = "Use measurement for radius";
//        public static final String MEASUREMENT_NAME = "Measurement name";

    }
}