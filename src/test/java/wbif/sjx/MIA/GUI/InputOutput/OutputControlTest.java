package wbif.sjx.MIA.GUI.InputOutput;

import org.junit.Test;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 26/06/2018.
 */
public class OutputControlTest extends ModuleTest {
    @Test
    public void testGetTitle() {
        assertNotNull(new OutputControl(null).getTitle());
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new OutputControl(null).getHelp());
    }
}