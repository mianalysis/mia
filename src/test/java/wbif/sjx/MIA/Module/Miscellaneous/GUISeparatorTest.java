package wbif.sjx.MIA.Module.Miscellaneous;

import org.junit.jupiter.api.BeforeAll;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class GUISeparatorTest extends ModuleTest {
    @BeforeAll
    public static void setVerbose() {
        Module.setVerbose(false);
    }

    @Override
    public void testGetHelp() {
        assertNotNull(new GUISeparator(null).getDescription());

    }
}