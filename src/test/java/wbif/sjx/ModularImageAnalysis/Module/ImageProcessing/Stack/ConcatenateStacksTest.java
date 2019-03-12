package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class ConcatenateStacksTest extends ModuleTest {

    @Override
    public void testGetTitle() {
        assertNotNull(new ConcatenateStacks<>().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new ConcatenateStacks<>().getHelp());
    }
}