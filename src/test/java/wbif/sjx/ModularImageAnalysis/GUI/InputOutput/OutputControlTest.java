package wbif.sjx.ModularImageAnalysis.GUI.InputOutput;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by sc13967 on 26/06/2018.
 */
public class OutputControlTest {
    @Test
    public void testGetTitle() {
        assertNotNull(new OutputControl().getTitle());
    }
}