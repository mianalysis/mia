package wbif.sjx.MIA.Module.Hidden;

import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by sc13967 on 26/06/2018.
 */
public class OutputControlTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new OutputControl(null).getDescription());
    }
}