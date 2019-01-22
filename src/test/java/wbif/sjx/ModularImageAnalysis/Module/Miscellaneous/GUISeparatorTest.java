package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import static org.junit.Assert.*;

public class GUISeparatorTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new GUISeparator().getTitle());

    }
}