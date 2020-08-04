package wbif.sjx.MIA.Module.WorkflowHandling;

import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowHandlingTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new WorkflowHandling(null).getDescription());
    }
}