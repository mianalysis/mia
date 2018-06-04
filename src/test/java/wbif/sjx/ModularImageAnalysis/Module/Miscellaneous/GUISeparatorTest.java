package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import org.junit.Test;

import static org.junit.Assert.*;

public class GUISeparatorTest {
    @Test
    public void testGetTitle() throws Exception {
        assertNotNull(new GUISeparator().getTitle());

    }
}