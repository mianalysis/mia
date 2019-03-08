package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import org.junit.BeforeClass;
import org.junit.Test;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ModuleTest;

import static org.junit.Assert.*;

public class GUISeparatorTest extends ModuleTest {
    @BeforeClass
    public static void setVerbose() {
        Module.setVerbose(true);
    }

    @Override
    public void testGetTitle() {
        assertNotNull(new GUISeparator().getTitle());

    }

    @Override
    public void testGetHelp() {
        assertNotNull(new GUISeparator().getHelp());

    }
}