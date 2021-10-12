package io.github.mianalysis.mia.module.workflow;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class WorkflowHandlingTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new WorkflowHandling(null).getDescription());
    }
}