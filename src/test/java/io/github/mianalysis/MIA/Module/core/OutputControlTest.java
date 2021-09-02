package io.github.mianalysis.mia.module.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.module.ModuleTest;

/**
 * Created by sc13967 on 26/06/2018.
 */
public class OutputControlTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new OutputControl(null).getDescription());
    }
}