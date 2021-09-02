package io.github.mianalysis.mia.module.imageprocessing.pixel.Binary;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class DilateErodeTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new DilateErode(null).getDescription());
    }
}