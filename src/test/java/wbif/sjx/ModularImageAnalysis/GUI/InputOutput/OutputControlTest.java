package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 26/06/2018.
 */
public class OutputControlTest extends ModuleTest {
    @Test
    public void testGetTitle() {
        assertNotNull(new OutputControl().getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new OutputControl().getHelp());
    }
}