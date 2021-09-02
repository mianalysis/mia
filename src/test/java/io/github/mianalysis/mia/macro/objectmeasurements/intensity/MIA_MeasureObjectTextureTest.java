package io.github.mianalysis.mia.macro.objectmeasurements.intensity;

import io.github.mianalysis.mia.macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_MeasureObjectTextureTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_MeasureObjectTexture(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_MeasureObjectTexture(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_MeasureObjectTexture(null).getDescription());
    }
}