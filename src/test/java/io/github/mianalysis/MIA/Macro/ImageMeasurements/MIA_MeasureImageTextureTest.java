package io.github.mianalysis.mia.macro.imagemeasurements;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.mianalysis.mia.macro.MacroOperationTest;

public class MIA_MeasureImageTextureTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_MeasureImageTexture(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_MeasureImageTexture(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_MeasureImageTexture(null).getDescription());
    }
}