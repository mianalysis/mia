package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class CreateOrthogonalViewTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new CreateOrthogonalView<>().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new CreateOrthogonalView<>().getHelp());
    }
}