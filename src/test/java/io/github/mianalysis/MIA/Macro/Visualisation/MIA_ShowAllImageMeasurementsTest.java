package io.github.mianalysis.MIA.Macro.Visualisation;

import io.github.mianalysis.MIA.Macro.MacroOperationTest;

import static org.junit.jupiter.api.Assertions.*;

public class MIA_ShowAllImageMeasurementsTest extends MacroOperationTest {

    @Override
    public void testGetName() {
        assertNotNull(new MIA_ShowAllImageMeasurements(null).getName());
    }

    @Override
    public void testGetArgumentsDescription() {
        assertNotNull(new MIA_ShowAllImageMeasurements(null).getArgumentsDescription());
    }

    @Override
    public void testGetDescription() {
        assertNotNull(new MIA_ShowAllImageMeasurements(null).getDescription());
    }
}