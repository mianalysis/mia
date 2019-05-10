package wbif.sjx.MIA.Module.Miscellaneous;

import org.junit.BeforeClass;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.Assert.*;

public class GUISeparatorTest extends ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new GUISeparator(null).getTitle());

    }

    @Override
    public void testGetHelp() {
        assertNotNull(new GUISeparator(null).getHelp());

    }
}