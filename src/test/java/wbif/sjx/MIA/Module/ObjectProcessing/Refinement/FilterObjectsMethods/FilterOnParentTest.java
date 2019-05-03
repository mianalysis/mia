package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjectsMethods;

import org.junit.Test;
import wbif.sjx.MIA.ExpectedObjects.ExpectedObjects;
import wbif.sjx.MIA.ExpectedObjects.Objects3D;
import wbif.sjx.MIA.Module.ModuleTest;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

import static org.junit.Assert.*;

public class FilterOnParentTest extends ModuleTest {
    @Override
    public void testGetTitle() {
        assertNotNull(new FilterOnParent().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new FilterOnParent().getHelp());
    }

    @Test
    public void testRunMissingParentRemove() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj", ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,false,true};
        ObjCollection parentObjects = new ObjCollection("Parents");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjectsMethods module
        FilterOnParent filterOnParent = new FilterOnParent();
        filterOnParent.updateParameterValue(FilterOnParent.INPUT_OBJECTS,"TestObj");
        filterOnParent.updateParameterValue(FilterOnParent.FILTER_METHOD,FilterOnParent.FilterMethods.WITHOUT_PARENT);
        filterOnParent.updateParameterValue(FilterOnParent.PARENT_OBJECT,"Parents");

        // Running the module
        filterOnParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(4,workspace.getObjectSet("TestObj").values().size());

    }

    @Test
    public void testRunWithParent() throws Exception {
        // Creating a new workspace
        Workspace workspace = new Workspace(0,null,1);

        // Setting calibration parameters
        double dppXY = 0.02;
        double dppZ = 0.1;
        String calibratedUnits = "µm";

        // Getting test objects
        ObjCollection testObjects = new Objects3D().getObjects("TestObj",ExpectedObjects.Mode.EIGHT_BIT,dppXY,dppZ,calibratedUnits,true);
        workspace.addObjects(testObjects);

        // Creating a second set of objects and relate these to the test objects.
        boolean[] parents = new boolean[]{true,true,false,true,false,false,true,true};
        ObjCollection parentObjects = new ObjCollection("Parents");

        int counter = 0;
        for (Obj testObject:testObjects.values()) {
            if (parents[counter++]) {
                Obj parentObject = new Obj("Parents", parentObjects.getAndIncrementID(), dppXY, dppZ, calibratedUnits,false);
                parentObjects.add(parentObject);

                testObject.addParent(parentObject);
                parentObject.addChild(testObject);

            }
        }

        // Initialising FilterObjectsMethods module
        FilterOnParent filterOnParent = new FilterOnParent();
        filterOnParent.updateParameterValue(FilterOnParent.INPUT_OBJECTS,"TestObj");
        filterOnParent.updateParameterValue(FilterOnParent.FILTER_METHOD,FilterOnParent.FilterMethods.WITH_PARENT);
        filterOnParent.updateParameterValue(FilterOnParent.PARENT_OBJECT,"Parents");

        // Running the module
        filterOnParent.execute(workspace);

        // Checking basic facts
        assertNotNull(workspace.getObjectSet("TestObj"));
        assertEquals(3,workspace.getObjectSet("TestObj").values().size());

    }
}