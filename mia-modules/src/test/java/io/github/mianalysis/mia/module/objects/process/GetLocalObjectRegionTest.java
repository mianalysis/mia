package io.github.mianalysis.mia.module.objects.process;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.mianalysis.mia.expectedobjects.ExpectedObjects;
import io.github.mianalysis.mia.expectedobjects.Spots3D;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleTest;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.coordinates.volume.VolumeType;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;


public class GetLocalObjectRegionTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new GetLocalObjectRegion(null).getDescription());
    }

    @ParameterizedTest
    @EnumSource(VolumeType.class)
    @Disabled
    public void testRun(VolumeType volumeType) throws IntegerOverflowException {
        // Creating a new workspace
        Workspaces workspaces = new Workspaces();
        Workspace workspace = workspaces.getNewWorkspace(null,1);

        // Setting object parameters
        String inputObjectsName = "Test objects";
        String outputObjectsName = "Output_objects";
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Creating objects and adding to workspace
        Objs testObjects = new Spots3D(volumeType).getObjects(inputObjectsName, ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
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