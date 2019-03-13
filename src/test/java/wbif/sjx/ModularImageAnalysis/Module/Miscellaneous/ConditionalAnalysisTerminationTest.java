package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ConditionalAnalysisTerminationTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ConditionalAnalysisTermination().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ConditionalAnalysisTermination().getHelp());
    }
}