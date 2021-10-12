package io.github.mianalysis.mia.module.images.transform.registration;

import io.github.mianalysis.mia.module.ModuleTest;

import static org.junit.jupiter.api.Assertions.*;


public class UnwarpImagesTest extends ModuleTest {

    @Override
    public void testGetHelp() {
        assertNotNull(new UnwarpAutomatic(null).getDescription());
    }
}