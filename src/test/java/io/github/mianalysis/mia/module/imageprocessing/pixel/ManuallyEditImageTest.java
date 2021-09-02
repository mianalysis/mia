package io.github.mianalysis.mia.module.imageprocessing.pixel;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;

public class ManuallyEditImageTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new ManuallyEditImage(null).getDescription());
    }
}