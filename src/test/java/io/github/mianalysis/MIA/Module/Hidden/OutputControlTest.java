package io.github.mianalysis.MIA.Module.Hidden;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.MIA.Module.ModuleTest;
import io.github.mianalysis.MIA.Module.Core.OutputControl;

/**
 * Created by sc13967 on 26/06/2018.
 */
public class OutputControlTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new OutputControl(null).getDescription());
    }
}