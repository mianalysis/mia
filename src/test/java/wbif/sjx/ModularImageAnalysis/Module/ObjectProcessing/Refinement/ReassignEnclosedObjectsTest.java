package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ReassignEnclosedObjectsTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ReassignEnclosedObjects().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ReassignEnclosedObjects().getHelp());
    }
}