package wbif.sjx.MIA.Macro.ObjectMeasurements.Intensity;

import wbif.sjx.MIA.Macro.MacroOperationTest;

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